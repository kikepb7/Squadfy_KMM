package com.kikepb.globalPosition.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.usecase.EnrollUseCase
import com.kikepb.club.domain.usecase.FetchMyClubsUseCase
import com.kikepb.club.domain.usecase.GetCurrentAnnouncementUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.usecase.ObserveMyClubsUseCase
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.globalPosition.domain.usecase.GetLatestNewsUseCase
import com.kikepb.globalPosition.domain.usecase.GetRecentMatchesUseCase
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnClubClick
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnCopyInviteCode
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnSettingsClick
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.CopyToClipboard
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToClub
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToSettings
import com.kikepb.globalPosition.presentation.home.HomeAnnouncementStatus
import com.kikepb.globalPosition.presentation.home.HomeClubCardModel
import com.kikepb.globalPosition.presentation.home.sortedForHome
import com.kikepb.globalPosition.presentation.mapper.toUiModel
import com.kikepb.globalPosition.presentation.model.MatchUiModel
import com.kikepb.globalPosition.presentation.model.NewsUiModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Clock
import kotlin.time.Instant

/** Home: my clubs with the state of each current announcement (spec 010). Network-first per club (ADR-0006). */
class GlobalPositionViewModel(
    observeMyClubsUseCase: ObserveMyClubsUseCase,
    private val fetchMyClubsUseCase: FetchMyClubsUseCase,
    private val getCurrentAnnouncementUseCase: GetCurrentAnnouncementUseCase,
    private val getScheduleUseCase: GetScheduleUseCase,
    private val enrollUseCase: EnrollUseCase,
    private val getRecentMatchesUseCase: GetRecentMatchesUseCase,
    private val getLatestNewsUseCase: GetLatestNewsUseCase,
    private val featureFlags: FeatureFlags,
    private val clock: Clock
) : ViewModel() {

    private val _state = MutableStateFlow(GlobalPositionUiState(now = clock.now()))

    /** Per club: announcement status, loaded at most [MAX_PARALLEL] at a time (AC-010-01). */
    private val statuses = MutableStateFlow<Map<String, HomeAnnouncementStatus>>(emptyMap())
    private val enrolling = MutableStateFlow<Set<String>>(emptySet())
    private val semaphore = Semaphore(MAX_PARALLEL)
    private var knownClubs: List<ClubModel> = emptyList()

    val state = combine(
        _state,
        observeMyClubsUseCase(),
        statuses,
        enrolling,
        combine(featureFlags.observe(FeatureFlag.HOME_RECENT_MATCHES), featureFlags.observe(FeatureFlag.HOME_NEWS), ::Pair)
    ) { current, myClubs, statusByClub, enrollingIds, (showMatches, showNews) ->
        val clubs = myClubs.map { it.club }
        onClubsChanged(clubs)
        current.copy(
            cards = clubs
                .map { club -> HomeClubCardModel(club, statusByClub[club.id] ?: HomeAnnouncementStatus.Loading, club.id in enrollingIds) }
                .sortedForHome(current.now),
            isLoadingClubs = false,
            showRecentMatches = showMatches,
            showNews = showNews
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = _state.value
    )

    private val eventChannel = Channel<GlobalPositionEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        refresh()
        loadMatchesAndNews()
    }

    fun onAction(action: GlobalPositionAction) {
        when (action) {
            is OnCopyInviteCode -> viewModelScope.launch { eventChannel.send(element = CopyToClipboard(action.code)) }
            OnSettingsClick -> viewModelScope.launch { eventChannel.send(element = NavigateToSettings) }
            is OnClubClick -> viewModelScope.launch { eventChannel.send(element = NavigateToClub(action.clubId)) }
            GlobalPositionAction.OnRefresh, GlobalPositionAction.OnResume -> refresh()
            is GlobalPositionAction.OnRetryClub -> knownClubs.firstOrNull { it.id == action.clubId }?.let(::loadAnnouncement)
            is GlobalPositionAction.OnEnrollClick -> enroll(action.clubId)
        }
    }

    /** New clubs (joined or created elsewhere) get their announcement loaded once. */
    private fun onClubsChanged(clubs: List<ClubModel>) {
        val newClubs = clubs.filter { club -> knownClubs.none { it.id == club.id } }
        knownClubs = clubs
        newClubs.forEach(::loadAnnouncement)
    }

    /** AC-010-07: pull-to-refresh and coming back to the foreground. */
    private fun refresh() {
        _state.update { it.copy(isRefreshing = true, now = clock.now()) }
        viewModelScope.launch {
            fetchMyClubsUseCase()
            knownClubs.forEach(::loadAnnouncement)
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun loadAnnouncement(club: ClubModel) {
        viewModelScope.launch {
            semaphore.withPermit {
                val status = when (val result = getCurrentAnnouncementUseCase(club.id)) {
                    is Result.Success -> result.data?.let { current ->
                        val zone = (getScheduleUseCase(club.id) as? Result.Success)?.data?.timeZone
                        HomeAnnouncementStatus.Loaded(current, zone)
                    } ?: HomeAnnouncementStatus.NoMatch
                    // AC-010-06: only this card shows the error
                    is Result.Failure -> HomeAnnouncementStatus.Unavailable
                }
                statuses.update { it + (club.id to status) }
            }
        }
    }

    private fun enroll(clubId: String) {
        val current = (statuses.value[clubId] as? HomeAnnouncementStatus.Loaded)?.current ?: return
        if (clubId in enrolling.value) return
        enrolling.update { it + clubId }
        viewModelScope.launch {
            enrollUseCase(current.announcement.id)
                .onFailure { eventChannel.send(GlobalPositionEvent.EnrollFailed) }
            // My status (confirmed or waitlisted) comes from the backend
            knownClubs.firstOrNull { it.id == clubId }?.let(::loadAnnouncement)
            enrolling.update { it - clubId }
        }
    }

    // Both sections are sample data; with their flag off nothing is requested (APP-RN-17)
    private fun loadMatchesAndNews() {
        if (featureFlags.isEnabled(FeatureFlag.HOME_RECENT_MATCHES)) viewModelScope.launch {
            getRecentMatchesUseCase()
                .onSuccess { matches -> _state.update { it.copy(matches = matches.map { m -> m.toUiModel() }) } }
        }
        if (featureFlags.isEnabled(FeatureFlag.HOME_NEWS)) viewModelScope.launch {
            getLatestNewsUseCase()
                .onSuccess { news -> _state.update { it.copy(news = news.map { n -> n.toUiModel() }) } }
        }
    }

    private companion object {
        const val MAX_PARALLEL = 4
    }
}

data class GlobalPositionUiState(
    val now: Instant,
    val cards: List<HomeClubCardModel> = emptyList(),
    val matches: List<MatchUiModel> = emptyList(),
    val news: List<NewsUiModel> = emptyList(),
    val isLoadingClubs: Boolean = true,
    val isRefreshing: Boolean = false,
    val showRecentMatches: Boolean = false,
    val showNews: Boolean = false
)

sealed interface GlobalPositionAction {
    data class OnCopyInviteCode(val code: String) : GlobalPositionAction
    data class OnClubClick(val clubId: String) : GlobalPositionAction
    data object OnSettingsClick : GlobalPositionAction
    data object OnRefresh : GlobalPositionAction
    data object OnResume : GlobalPositionAction
    data class OnRetryClub(val clubId: String) : GlobalPositionAction
    data class OnEnrollClick(val clubId: String) : GlobalPositionAction
}

sealed interface GlobalPositionEvent {
    data class CopyToClipboard(val code: String) : GlobalPositionEvent
    data class NavigateToClub(val clubId: String) : GlobalPositionEvent
    data object NavigateToSettings : GlobalPositionEvent
    data object EnrollFailed : GlobalPositionEvent
}
