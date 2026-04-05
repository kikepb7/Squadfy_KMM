package com.kikepb.globalPosition.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.globalPosition.domain.usecase.GetLatestNewsUseCase
import com.kikepb.globalPosition.domain.usecase.GetRecentMatchesUseCase
import com.kikepb.globalPosition.domain.usecase.GetUserClubsUseCase
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnCopyInviteCode
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnSettingsClick
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.CopyToClipboard
import com.kikepb.globalPosition.presentation.mapper.toUiModel
import com.kikepb.globalPosition.presentation.model.ClubUiModel
import com.kikepb.globalPosition.presentation.model.MatchUiModel
import com.kikepb.globalPosition.presentation.model.NewsUiModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GlobalPositionViewModel(
    private val getUserClubsUseCase: GetUserClubsUseCase,
    private val getRecentMatchesUseCase: GetRecentMatchesUseCase,
    private val getLatestNewsUseCase: GetLatestNewsUseCase,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(GlobalPositionUiState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadData()
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
            is OnCopyInviteCode -> { viewModelScope.launch { eventChannel.send(CopyToClipboard(action.code)) } }
            OnSettingsClick -> { viewModelScope.launch { eventChannel.send(GlobalPositionEvent.NavigateToSettings) } }
        }
    }

    private fun loadData() {

        // TODO --> Combine in one use case and use flows
        viewModelScope.launch {
            getUserClubsUseCase()
                .onSuccess { clubs ->
                    _state.update { it.copy(clubs = clubs.map { club -> club.toUiModel() }, isLoadingClubs = false) }
                }
                .onFailure { _state.update { it.copy(isLoadingClubs = false) } }
        }
        viewModelScope.launch {
            getRecentMatchesUseCase()
                .onSuccess { matches ->
                    _state.update { it.copy(matches = matches.map { match -> match.toUiModel() }, isLoadingMatches = false) }
                }
                .onFailure { _state.update { it.copy(isLoadingMatches = false) } }
        }
        viewModelScope.launch {
            getLatestNewsUseCase()
                .onSuccess { news ->
                    _state.update { it.copy(news = news.map { item -> item.toUiModel() }, isLoadingNews = false) }
                }
                .onFailure {
                    _state.update { it.copy(isLoadingNews = false) }
                }
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
    data object OnSettingsClick : GlobalPositionAction
}

sealed interface GlobalPositionEvent {
    data class CopyToClipboard(val code: String) : GlobalPositionEvent
    data object NavigateToSettings : GlobalPositionEvent
}
