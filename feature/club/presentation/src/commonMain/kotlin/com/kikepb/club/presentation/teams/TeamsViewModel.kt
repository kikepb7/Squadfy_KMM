package com.kikepb.club.presentation.teams

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.TeamBalanceModel
import com.kikepb.core.domain.realtime.ClubDataScope
import com.kikepb.core.domain.realtime.ClubLiveUpdates
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.GetCurrentAnnouncementUseCase
import com.kikepb.club.domain.usecase.GetMatchUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.usecase.GetTeamBalanceUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/**
 * Equipos tab (spec 018, Equipos B): the teams of the current match on the pitch or as lists, and the balance when
 * the backend shares it (managers, BE-003 RN-10). Read-only: editing teams stays in the match detail (spec 006).
 * Loading follows spec 017: only a pull shows the indicator and the latest refresh wins.
 */
class TeamsViewModel(
    private val getCurrentAnnouncementUseCase: GetCurrentAnnouncementUseCase,
    private val getMatchUseCase: GetMatchUseCase,
    private val getTeamBalanceUseCase: GetTeamBalanceUseCase,
    private val getScheduleUseCase: GetScheduleUseCase,
    getClubMembersUseCase: GetClubMembersUseCase,
    observeMyMembershipUseCase: ObserveMyMembershipUseCase,
    private val clubLiveUpdates: ClubLiveUpdates,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId") ?: throw IllegalStateException("clubId is required")

    private val _state = MutableStateFlow(TeamsState())
    // Declared before init: an initializer placed after it would reset the job started there (spec 017)
    private var loadJob: Job? = null

    init {
        load(userInitiated = false)
        viewModelScope.launch {
            clubLiveUpdates.observe(clubId).collect { change ->
                if (change.scope == ClubDataScope.MATCH) load(userInitiated = false)
            }
        }
    }

    val state = combine(
        _state,
        getClubMembersUseCase(clubId),
        observeMyMembershipUseCase(clubId)
    ) { current, members, me ->
        current.copy(members = members.associateBy { it.id }, myMemberId = me?.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), TeamsState())

    fun onAction(action: TeamsAction) {
        when (action) {
            TeamsAction.OnRefresh -> load(userInitiated = true)
            TeamsAction.OnResume -> load(userInitiated = false)
            is TeamsAction.OnViewSelected -> _state.update { it.copy(view = action.view) }
        }
    }

    private fun load(userInitiated: Boolean) {
        loadJob?.cancel()
        _state.update { it.copy(isRefreshing = userInitiated, loadFailed = false) }
        loadJob = viewModelScope.launch {
            try {
                when (val current = getCurrentAnnouncementUseCase(clubId)) {
                    is Result.Failure -> _state.update { it.copy(loadFailed = !it.hasLoaded) }
                    is Result.Success -> {
                        val announcement = current.data?.announcement
                        val match = announcement?.let { (getMatchUseCase(it.matchId) as? Result.Success)?.data }
                        val balance = match?.takeIf { it.hasTeams }?.let { (getTeamBalanceUseCase(it.id) as? Result.Success)?.data }
                        val zone = (getScheduleUseCase(clubId) as? Result.Success)?.data?.timeZone
                        _state.update {
                            it.copy(
                                hasLoaded = true,
                                hasMatch = announcement != null,
                                match = match,
                                drawAt = announcement?.drawAt,
                                balance = balance,
                                timeZoneId = zone ?: it.timeZoneId
                            )
                        }
                    }
                }
            } finally {
                _state.update { it.copy(isRefreshing = false) }
            }
        }
    }
}

enum class TeamsView { PITCH, LIST }

data class TeamsState(
    val hasLoaded: Boolean = false,
    val loadFailed: Boolean = false,
    val isRefreshing: Boolean = false,
    /** There is a current announcement (with or without teams yet). */
    val hasMatch: Boolean = false,
    val match: MatchModel? = null,
    val drawAt: Instant? = null,
    val balance: TeamBalanceModel? = null,
    val members: Map<String, ClubMemberModel> = emptyMap(),
    val myMemberId: String? = null,
    val timeZoneId: String = TimeZone.currentSystemDefault().id,
    val view: TeamsView = TeamsView.PITCH
) {
    val hasTeams: Boolean get() = match?.hasTeams == true
}

sealed interface TeamsAction {
    data object OnRefresh : TeamsAction
    data object OnResume : TeamsAction
    data class OnViewSelected(val view: TeamsView) : TeamsAction
}
