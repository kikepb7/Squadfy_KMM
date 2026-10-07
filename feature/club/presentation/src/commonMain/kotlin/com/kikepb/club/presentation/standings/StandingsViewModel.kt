package com.kikepb.club.presentation.standings

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
import com.kikepb.club.domain.usecase.noCompletedMatches
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Rating and stats classifications of a club (spec 008). Network-first: reloaded on open and on resume (ADR-0006). */
class StandingsViewModel(
    getClubMembersUseCase: GetClubMembersUseCase,
    observeMyMembershipUseCase: ObserveMyMembershipUseCase,
    private val getRatingLeaderboardUseCase: GetRatingLeaderboardUseCase,
    private val getMyRatingUseCase: GetMyRatingUseCase,
    private val getStatsLeaderboardUseCase: GetStatsLeaderboardUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")

    private val _state = MutableStateFlow(StandingsState())

    val state = combine(_state, getClubMembersUseCase(clubId), observeMyMembershipUseCase(clubId)) { current, members, me ->
        current.copy(members = members.associateBy { it.id }, myMemberId = me?.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = _state.value
    )

    init {
        refresh()
    }

    fun onAction(action: StandingsAction) {
        when (action) {
            StandingsAction.OnRefresh -> refresh()
            is StandingsAction.OnModeSelected -> _state.update { it.copy(mode = action.mode) }
            is StandingsAction.OnSortSelected -> {
                _state.update { it.copy(sortBy = action.sortBy) }
                loadStats()
            }
        }
    }

    private fun refresh() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getRatingLeaderboardUseCase(clubId)
                .onSuccess { ratings -> _state.update { it.copy(ratings = ratings, isStale = false) } }
                .onFailure(::onError)
            // 404 when I am not rated yet: the "your position" card is simply hidden
            getMyRatingUseCase(clubId).onSuccess { mine -> _state.update { it.copy(myRating = mine) } }
            loadStatsNow()
            _state.update { it.copy(isLoading = false, hasLoaded = true) }
        }
    }

    private fun loadStats() {
        viewModelScope.launch { loadStatsNow() }
    }

    private suspend fun loadStatsNow() {
        getStatsLeaderboardUseCase(clubId, _state.value.sortBy)
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
}
