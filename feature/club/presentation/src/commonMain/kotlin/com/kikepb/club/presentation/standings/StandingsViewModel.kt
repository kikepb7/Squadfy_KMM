package com.kikepb.club.presentation.standings

import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.usecase.GetClubMatchesUseCase
import com.kikepb.club.domain.usecase.GetSeasonsUseCase
import com.kikepb.club.domain.model.SeasonModel
import com.kikepb.club.domain.model.SeasonStatus
import com.kikepb.club.domain.model.current
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

/**
 * Rating and stats classifications of a club (spec 008). Network-first: reloaded on open and on resume (ADR-0006).
 * Per season (spec 020 APP-RN-S4/S6): the current one first, and the closed ones with their final standings.
 */
class StandingsViewModel(
    private val getClubMembersUseCase: GetClubMembersUseCase,
    observeMyMembershipUseCase: ObserveMyMembershipUseCase,
    private val getRatingLeaderboardUseCase: GetRatingLeaderboardUseCase,
    private val getMyRatingUseCase: GetMyRatingUseCase,
    private val getStatsLeaderboardUseCase: GetStatsLeaderboardUseCase,
    private val syncClubDetailUseCase: SyncClubDetailUseCase,
    private val getScheduleUseCase: GetScheduleUseCase,
    private val clock: Clock,
    private val getClubMatchesUseCase: GetClubMatchesUseCase,
    private val getSeasonsUseCase: GetSeasonsUseCase,
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
            is StandingsAction.OnSeasonSelected -> {
                if (action.seasonId == _state.value.selectedSeasonId) return
                _state.update { it.copy(selectedSeasonId = action.seasonId, statsPeriod = StatsPeriod.ALL_TIME, ratings = emptyList(), myRating = null) }
                refresh()
            }
        }
    }

    /** Spec 017: only a pull shows the indicator ([StandingsState.isLoading]); the first load uses `hasLoaded`. */
    private fun refresh(userInitiated: Boolean = false) {
        refreshJob?.cancel()
        if (userInitiated) _state.update { it.copy(isLoading = true) }
        refreshJob = viewModelScope.launch {
            try {
                // Spec 020: the seasons first; the own backend has none and everything works as before
                getSeasonsUseCase(clubId).onSuccess { seasons ->
                    _state.update { state ->
                        state.copy(
                            seasons = seasons,
                            selectedSeasonId = state.selectedSeasonId?.takeIf { id -> seasons.any { it.id == id } } ?: seasons.current()?.id
                        )
                    }
                }
                val seasonId = _state.value.selectedSeasonId
                getRatingLeaderboardUseCase(clubId, seasonId)
                    .onSuccess { ratings ->
                        _state.update { it.copy(ratings = ratings, isStale = false) }
                        // APP-RN-06: rows of members who joined after the last members sync
                        val cached = getClubMembersUseCase(clubId).first().map { it.id }.toSet()
                        if (ratings.any { it.clubMemberId !in cached }) syncClubDetailUseCase(clubId)
                    }
                    .onFailure(::onError)
                // 404 when I am not rated yet: the "your position" card is simply hidden
                getMyRatingUseCase(clubId, seasonId).onSuccess { mine -> _state.update { it.copy(myRating = mine) } }
                // Spec 018: each row shows the change of the last completed match (BE-008 RN-F1), only in the season in progress
                if (_state.value.isCurrentSeason) {
                    getClubMatchesUseCase(clubId, MatchStatus.COMPLETED).onSuccess { matches ->
                        val last = matches.maxByOrNull { it.scheduledAt }
                        _state.update { it.copy(lastMatchDeltas = last?.ratingChanges.orEmpty()) }
                    }
                } else {
                    _state.update { it.copy(lastMatchDeltas = emptyMap()) }
                }
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
        getStatsLeaderboardUseCase(clubId, _state.value.sortBy, _state.value.statsPeriod, today, _state.value.selectedSeasonId)
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
    val myMemberId: String? = null,
    /** Rating change of each member in the last completed match (spec 018, the ▲/▼ of the table). */
    val lastMatchDeltas: Map<String, Int> = emptyMap(),
    /** Spec 020: newest first; empty with the own backend, which has no seasons. */
    val seasons: List<SeasonModel> = emptyList(),
    val selectedSeasonId: String? = null
) {
    val selectedSeason: SeasonModel? get() = seasons.firstOrNull { it.id == selectedSeasonId }

    /** The season in progress (or the pre-season one), the default selection. */
    val isCurrentSeason: Boolean get() = selectedSeasonId == null || selectedSeasonId == seasons.current()?.id

    /** AC-020-02: the selected season has not started yet. */
    val isPreSeason: Boolean get() = selectedSeason?.status == SeasonStatus.UPCOMING

    /** AC-008-06 */
    val isEmpty: Boolean get() = hasLoaded && !isPreSeason && ratings.noCompletedMatches()
}

sealed interface StandingsAction {
    data object OnRefresh : StandingsAction
    data class OnModeSelected(val mode: StandingsMode) : StandingsAction
    data class OnSortSelected(val sortBy: StatsSortBy) : StandingsAction
    data class OnStatsPeriodSelected(val period: StatsPeriod) : StandingsAction
    data class OnSeasonSelected(val seasonId: String) : StandingsAction
}
