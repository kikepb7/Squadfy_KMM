package com.kikepb.globalPosition.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.globalPosition.domain.model.ClubModel
import com.kikepb.globalPosition.domain.model.MatchModel
import com.kikepb.globalPosition.domain.usecase.FetchUserClubsUseCase
import com.kikepb.globalPosition.domain.usecase.GetCurrentUserUseCase
import com.kikepb.globalPosition.domain.usecase.GetLatestNewsUseCase
import com.kikepb.globalPosition.domain.usecase.GetNextMatchUseCase
import com.kikepb.globalPosition.domain.usecase.GetRecentMatchesUseCase
import com.kikepb.globalPosition.domain.usecase.GetUserClubsUseCase
import com.kikepb.globalPosition.domain.usecase.ToggleMatchParticipationUseCase
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnClubClick
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnCopyInviteCode
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnJoinClubClick
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnSettingsClick
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnToggleMatchParticipation
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.CopyToClipboard
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToClub
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToJoinClub
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToSettings
import com.kikepb.globalPosition.presentation.mapper.toUiModel
import com.kikepb.globalPosition.presentation.model.ClubUiModel
import com.kikepb.globalPosition.presentation.model.MatchUiModel
import com.kikepb.globalPosition.presentation.model.NewsUiModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class GlobalPositionViewModel(
    private val fetchUserClubsUseCase: FetchUserClubsUseCase,
    private val toggleMatchParticipationUseCase: ToggleMatchParticipationUseCase,
    private val getRecentMatchesUseCase: GetRecentMatchesUseCase,
    private val getLatestNewsUseCase: GetLatestNewsUseCase,
    getUserClubsUseCase: GetUserClubsUseCase,
    getNextMatchUseCase: GetNextMatchUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(GlobalPositionInternalState())
    private val _rawRecentMatches = MutableStateFlow<List<MatchModel>>(emptyList())

    private val eventChannel = Channel<GlobalPositionEvent>()
    val events = eventChannel.receiveAsFlow()

    val state = combine(
        _state,
        getUserClubsUseCase(),
        getNextMatchUseCase(),
        getCurrentUserUseCase(),
        _rawRecentMatches
    ) { internal, clubs, nextMatchPair, user, rawMatches ->
        val clubsById: Map<String, ClubModel> = clubs.associateBy { it.id }
        GlobalPositionUiState(
            userName = user?.username ?: "",
            currentDate = formattedCurrentDate(),
            clubs = clubs.map { it.toUiModel() },
            nextMatch = nextMatchPair?.first?.toUiModel(clubsById),
            isJoinedNextMatch = nextMatchPair?.second ?: false,
            isTogglingParticipation = internal.isTogglingParticipation,
            recentMatches = rawMatches.map { it.toUiModel(clubsById) },
            news = internal.news,
            isLoadingClubs = internal.isLoadingClubs,
            isLoadingMatches = internal.isLoadingMatches,
            isLoadingNews = internal.isLoadingNews
        )
    }
        .onStart {
            if (!hasLoadedInitialData) {
                fetchClubs()
                loadMatchesAndNews()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
            initialValue = GlobalPositionUiState()
        )

    fun onAction(action: GlobalPositionAction) {
        when (action) {
            is OnCopyInviteCode -> viewModelScope.launch { eventChannel.send(CopyToClipboard(action.code)) }
            OnSettingsClick -> viewModelScope.launch { eventChannel.send(NavigateToSettings) }
            is OnClubClick -> viewModelScope.launch { eventChannel.send(NavigateToClub(action.clubId)) }
            is OnToggleMatchParticipation -> toggleParticipation(action.matchId)
            OnJoinClubClick -> viewModelScope.launch { eventChannel.send(NavigateToJoinClub) }
        }
    }

    private fun fetchClubs() {
        viewModelScope.launch { fetchUserClubsUseCase() }
    }

    private fun loadMatchesAndNews() {
        viewModelScope.launch {
            getRecentMatchesUseCase()
                .onSuccess { matches ->
                    _rawRecentMatches.value = matches.take(3)
                    _state.update { it.copy(isLoadingMatches = false) }
                }
                .onFailure { _state.update { it.copy(isLoadingMatches = false) } }
        }
        viewModelScope.launch {
            getLatestNewsUseCase()
                .onSuccess { news ->
                    _state.update {
                        it.copy(
                            news = news.take(3).map { n -> n.toUiModel() },
                            isLoadingNews = false
                        )
                    }
                }
                .onFailure { _state.update { it.copy(isLoadingNews = false) } }
        }
    }

    private fun toggleParticipation(matchId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isTogglingParticipation = true) }
            toggleMatchParticipationUseCase(matchId = matchId)
            _state.update { it.copy(isTogglingParticipation = false) }
        }
    }
}

private data class GlobalPositionInternalState(
    val isTogglingParticipation: Boolean = false,
    val news: List<NewsUiModel> = emptyList(),
    val isLoadingClubs: Boolean = true,
    val isLoadingMatches: Boolean = true,
    val isLoadingNews: Boolean = true
)

data class GlobalPositionUiState(
    val userName: String = "",
    val currentDate: String = "",
    val clubs: List<ClubUiModel> = emptyList(),
    val nextMatch: MatchUiModel? = null,
    val isJoinedNextMatch: Boolean = false,
    val isTogglingParticipation: Boolean = false,
    val recentMatches: List<MatchUiModel> = emptyList(),
    val news: List<NewsUiModel> = emptyList(),
    val isLoadingClubs: Boolean = true,
    val isLoadingMatches: Boolean = true,
    val isLoadingNews: Boolean = true
)

sealed interface GlobalPositionAction {
    data class OnCopyInviteCode(val code: String) : GlobalPositionAction
    data class OnClubClick(val clubId: String) : GlobalPositionAction
    data class OnToggleMatchParticipation(val matchId: String) : GlobalPositionAction
    data object OnSettingsClick : GlobalPositionAction
    data object OnJoinClubClick : GlobalPositionAction
}

sealed interface GlobalPositionEvent {
    data class CopyToClipboard(val code: String) : GlobalPositionEvent
    data class NavigateToClub(val clubId: String) : GlobalPositionEvent
    data object NavigateToSettings : GlobalPositionEvent
    data object NavigateToJoinClub : GlobalPositionEvent
}

private fun formattedCurrentDate(): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val dayName = when (today.dayOfWeek) {
        DayOfWeek.MONDAY -> "Lunes"
        DayOfWeek.TUESDAY -> "Martes"
        DayOfWeek.WEDNESDAY -> "Miércoles"
        DayOfWeek.THURSDAY -> "Jueves"
        DayOfWeek.FRIDAY -> "Viernes"
        DayOfWeek.SATURDAY -> "Sábado"
        DayOfWeek.SUNDAY -> "Domingo"
    }
    val monthName = when (today.month.number) {
        1 -> "enero"
        2 -> "febrero"
        3 -> "marzo"
        4 -> "abril"
        5 -> "mayo"
        6 -> "junio"
        7 -> "julio"
        8 -> "agosto"
        9 -> "septiembre"
        10 -> "octubre"
        11 -> "noviembre"
        12 -> "diciembre"
        else -> ""
    }
    return "$dayName, ${today.day} de $monthName"
}
