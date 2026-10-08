package com.kikepb.club.presentation.standings

import kotlinx.coroutines.Job
import kotlin.time.Clock
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.model.StatsPeriod
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.error.ClubError
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.MyRating
import com.kikepb.club.domain.model.RatingEntry
import com.kikepb.club.domain.model.StatsEntry
import com.kikepb.club.domain.model.StatsSortBy
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.GetMyRatingUseCase
import com.kikepb.club.domain.usecase.GetRatingLeaderboardUseCase
import com.kikepb.club.domain.usecase.GetStatsLeaderboardUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.domain.usecase.SyncClubDetailUseCase
import com.kikepb.club.domain.usecase.noCompletedMatches
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Rating and stats classifications of a club (spec 008). Network-first: reloaded on open and on resume (ADR-0006). */
class StandingsViewModel(
    private val getClubMembersUseCase: GetClubMembersUseCase,
    observeMyMembershipUseCase: ObserveMyMembershipUseCase,
    private val getRatingLeaderboardUseCase: GetRatingLeaderboardUseCase,
    private val getMyRatingUseCase: GetMyRatingUseCase,
    private val getStatsLeaderboardUseCase: GetStatsLeaderboardUseCase,
    private val syncClubDetailUseCase: SyncClubDetailUseCase,
    private val getScheduleUseCase: GetScheduleUseCase,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")

    private val _state = MutableStateFlow(StandingsState())
    // Declared before init: an initializer placed after it would reset the job started there (spec 017)
    private var refreshJob: Job? = null
    private var statsJob: Job? = null

    val state = combine(_state, getClubMembersUseCase(clubId), observeMyMembershipUseCase(clubId)) { current, members, me ->
        current.copy(members = members.associateBy { it.id }, myMemberId = me?.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = _state.value
    )

    /** APP-RN-03: the periods are those of the club time zone (from its schedule), the device one meanwhile. */
    private var clubTimeZone: TimeZone = TimeZone.currentSystemDefault()

    init {
        viewModelScope.launch {
            getScheduleUseCase(clubId).onSuccess { schedule ->
                schedule?.let { clubTimeZone = runCatching { TimeZone.of(it.timeZone) }.getOrDefault(clubTimeZone) }
            }
        }
        refresh()
    }

    fun onAction(action: StandingsAction) {
        when (action) {
            StandingsAction.OnRefresh -> refresh(userInitiated = true)
            is StandingsAction.OnModeSelected -> _state.update { it.copy(mode = action.mode) }
            is StandingsAction.OnSortSelected -> {
                _state.update { it.copy(sortBy = action.sortBy) }
                loadStats()
            }
            is StandingsAction.OnStatsPeriodSelected -> {
                _state.update { it.copy(statsPeriod = action.period) }
                loadStats()
            }
        }
    }

    /** Spec 017: only a pull shows the indicator ([StandingsState.isLoading]); the first load uses `hasLoaded`. */
    private fun refresh(userInitiated: Boolean = false) {
        refreshJob?.cancel()
        if (userInitiated) _state.update { it.copy(isLoading = true) }
        refreshJob = viewModelScope.launch {
            try {
                getRatingLeaderboardUseCase(clubId)
                    .onSuccess { ratings ->
                        _state.update { it.copy(ratings = ratings, isStale = false) }
                        // APP-RN-06: rows of members who joined after the last members sync
                        val cached = getClubMembersUseCase(clubId).first().map { it.id }.toSet()
                        if (ratings.any { it.clubMemberId !in cached }) syncClubDetailUseCase(clubId)
                    }
                    .onFailure(::onError)
                // 404 when I am not rated yet: the "your position" card is simply hidden
                getMyRatingUseCase(clubId).onSuccess { mine -> _state.update { it.copy(myRating = mine) } }
                loadStatsNow()
            } finally {
                _state.update { it.copy(isLoading = false, hasLoaded = true) }
            }
        }
    }

    /** Changing sort or period quickly: only the newest request may write the table. */
    private fun loadStats() {
        statsJob?.cancel()
        statsJob = viewModelScope.launch { loadStatsNow() }
    }

    private suspend fun loadStatsNow() {
        val today = clock.now().toLocalDateTime(clubTimeZone).date
        getStatsLeaderboardUseCase(clubId, _state.value.sortBy, _state.value.statsPeriod, today)
            .onSuccess { stats -> _state.update { it.copy(stats = stats) } }
            .onFailure(::onError)
    }

    private fun onError(error: ClubError) {
        if (error is ClubError.Remote && error.error.status == DataError.Remote.NO_INTERNET) _state.update { it.copy(isStale = true) }
    }
}

enum class StandingsMode { RATING, STATS }

data class StandingsState(
    val hasLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val mode: StandingsMode = StandingsMode.RATING,
    val sortBy: StatsSortBy = StatsSortBy.GOALS,
    val statsPeriod: StatsPeriod = StatsPeriod.ALL_TIME,
    val ratings: List<RatingEntry> = emptyList(),
    val myRating: MyRating? = null,
    val stats: List<StatsEntry> = emptyList(),
    val members: Map<String, ClubMemberModel> = emptyMap(),
    val myMemberId: String? = null
) {
    /** AC-008-06 */
    val isEmpty: Boolean get() = hasLoaded && ratings.noCompletedMatches()
}

sealed interface StandingsAction {
    data object OnRefresh : StandingsAction
    data class OnModeSelected(val mode: StandingsMode) : StandingsAction
    data class OnSortSelected(val sortBy: StatsSortBy) : StandingsAction
    data class OnStatsPeriodSelected(val period: StatsPeriod) : StandingsAction
}
