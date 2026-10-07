package com.kikepb.club.presentation.clubs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.MyClubModel
import com.kikepb.club.domain.usecase.FetchMyClubsUseCase
import com.kikepb.club.domain.usecase.ObserveMyClubsUseCase
import com.kikepb.club.presentation.mapper.toUiText
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** "My clubs" (spec 003, AC-003-01): Room cache refreshed from `GET /clubs`. */
class ClubsListViewModel(
    observeMyClubsUseCase: ObserveMyClubsUseCase,
    private val fetchMyClubsUseCase: FetchMyClubsUseCase
) : ViewModel() {

    private val eventChannel = Channel<ClubsListEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ClubsListState())

    val state = combine(_state, observeMyClubsUseCase()) { current, clubs ->
        current.copy(clubs = clubs, isLoading = false)
    }
        .onStart { refresh() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
            initialValue = ClubsListState()
        )

    fun onAction(action: ClubsListAction) {
        when (action) {
            ClubsListAction.OnRefresh -> refresh()
        }
    }

    private fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            fetchMyClubsUseCase().onFailure { error -> eventChannel.send(ClubsListEvent.ShowMessage(error.toUiText())) }
            _state.update { it.copy(isRefreshing = false) }
        }
    }
}

data class ClubsListState(
    val clubs: List<MyClubModel> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)

sealed interface ClubsListAction {
    data object OnRefresh : ClubsListAction
}

sealed interface ClubsListEvent {
    data class ShowMessage(val message: UiText) : ClubsListEvent
}
