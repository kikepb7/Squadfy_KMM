package com.kikepb.club.presentation.clubs.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.usecase.GetUserClubsUseCase
import com.kikepb.club.presentation.clubs.shared.ClubRefreshNotifier
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClubTeamsListViewModel(
    private val getUserClubsUseCase: GetUserClubsUseCase,
    private val clubRefreshNotifier: ClubRefreshNotifier
) : ViewModel() {

    private val eventChannel = Channel<ClubTeamsListEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ClubTeamsListState())
    val state = _state.asStateFlow()

    init {
        refreshClubs(showLoader = true)
        observeRefreshEvents()
    }

    fun onAction(action: ClubTeamsListAction) {
        when (action) {
            ClubTeamsListAction.OnRefresh -> refreshClubs(showLoader = true)
        }
    }

    private fun observeRefreshEvents() {
        viewModelScope.launch {
            clubRefreshNotifier.refreshEvents.collect {
                refreshClubs(showLoader = false)
            }
        }
    }

    private fun refreshClubs(showLoader: Boolean) {
        viewModelScope.launch {
            if (showLoader || state.value.clubs.isEmpty()) {
                _state.update { it.copy(isLoading = true) }
            }

            getUserClubsUseCase
                .getUserClubs()
                .onSuccess { clubs ->
                    _state.update { it.copy(isLoading = false, clubs = clubs) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(ClubTeamsListEvent.ShowMessage(error.toUiText()))
                }
        }
    }
}

data class ClubTeamsListState(
    val isLoading: Boolean = false,
    val clubs: List<ClubModel> = emptyList()
)

sealed interface ClubTeamsListAction {
    data object OnRefresh : ClubTeamsListAction
}

sealed interface ClubTeamsListEvent {
    data class ShowMessage(val message: UiText) : ClubTeamsListEvent
}
