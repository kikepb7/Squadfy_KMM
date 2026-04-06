package com.kikepb.globalPosition.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.globalPosition.domain.usecase.FetchUserClubsUseCase
import com.kikepb.globalPosition.domain.usecase.GetLatestNewsUseCase
import com.kikepb.globalPosition.domain.usecase.GetRecentMatchesUseCase
import com.kikepb.globalPosition.domain.usecase.GetUserClubsUseCase
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnClubClick
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnCopyInviteCode
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnSettingsClick
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.CopyToClipboard
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToClub
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

class GlobalPositionViewModel(
    private val getUserClubsUseCase: GetUserClubsUseCase,
    private val fetchUserClubsUseCase: FetchUserClubsUseCase,
    private val getRecentMatchesUseCase: GetRecentMatchesUseCase,
    private val getLatestNewsUseCase: GetLatestNewsUseCase,
) : ViewModel() {
    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(GlobalPositionUiState())

    val state = combine(
        flow = _state,
        flow2 = getUserClubsUseCase()
    ) { current, clubs ->
        current.copy(
            clubs = clubs.map { it.toUiModel() },
            isLoadingClubs = false
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

    private val eventChannel = Channel<GlobalPositionEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: GlobalPositionAction) {
        when (action) {
            is OnCopyInviteCode -> viewModelScope.launch { eventChannel.send(element = CopyToClipboard(action.code)) }
            OnSettingsClick -> viewModelScope.launch { eventChannel.send(element = NavigateToSettings) }
            is OnClubClick -> viewModelScope.launch { eventChannel.send(element = NavigateToClub(action.clubId)) }
        }
    }

    private fun fetchClubs() {
        viewModelScope.launch {
            fetchUserClubsUseCase()
        }
    }

    private fun loadMatchesAndNews() {
        viewModelScope.launch {
            getRecentMatchesUseCase()
                .onSuccess { matches ->
                    _state.update { it.copy(matches = matches.map { m -> m.toUiModel() }, isLoadingMatches = false) }
                }
                .onFailure { _state.update { it.copy(isLoadingMatches = false) } }
        }
        viewModelScope.launch {
            getLatestNewsUseCase()
                .onSuccess { news ->
                    _state.update { it.copy(news = news.map { n -> n.toUiModel() }, isLoadingNews = false) }
                }
                .onFailure { _state.update { it.copy(isLoadingNews = false) } }
        }
    }
}

data class GlobalPositionUiState(
    val clubs: List<ClubUiModel> = emptyList(),
    val matches: List<MatchUiModel> = emptyList(),
    val news: List<NewsUiModel> = emptyList(),
    val isLoadingClubs: Boolean = true,
    val isLoadingMatches: Boolean = true,
    val isLoadingNews: Boolean = true
)

sealed interface GlobalPositionAction {
    data class OnCopyInviteCode(val code: String) : GlobalPositionAction
    data class OnClubClick(val clubId: String) : GlobalPositionAction
    data object OnSettingsClick : GlobalPositionAction
}

sealed interface GlobalPositionEvent {
    data class CopyToClipboard(val code: String) : GlobalPositionEvent
    data class NavigateToClub(val clubId: String) : GlobalPositionEvent
    data object NavigateToSettings : GlobalPositionEvent
}
