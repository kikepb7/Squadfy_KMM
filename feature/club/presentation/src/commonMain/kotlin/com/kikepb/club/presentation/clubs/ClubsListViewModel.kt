package com.kikepb.club.presentation.clubs

import kotlinx.coroutines.Job
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
    // Declared before init: an initializer placed after it would reset the job started there (spec 017)
    private var refreshJob: Job? = null

    // Spec 017: fetched once per screen instance (not on every re-subscription), silently; an empty cache only
    // shows "no clubs" after that fetch answered
    init {
        refresh()
    }

    val state = combine(_state, observeMyClubsUseCase()) { current, clubs ->
        current.copy(clubs = clubs, isLoading = clubs.isEmpty() && !current.hasFetched)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
            initialValue = ClubsListState()
        )

    fun onAction(action: ClubsListAction) {
        when (action) {
            ClubsListAction.OnRefresh -> refresh(userInitiated = true)
        }
    }

    private fun refresh(userInitiated: Boolean = false) {
        refreshJob?.cancel()
        if (userInitiated) _state.update { it.copy(isRefreshing = true) }
        refreshJob = viewModelScope.launch {
            try {
                fetchMyClubsUseCase().onFailure { error -> eventChannel.send(ClubsListEvent.ShowMessage(error.toUiText())) }
            } finally {
                _state.update { it.copy(isRefreshing = false, hasFetched = true) }
            }
        }
    }
}

data class ClubsListState(
    val clubs: List<MyClubModel> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val hasFetched: Boolean = false
)

sealed interface ClubsListAction {
    data object OnRefresh : ClubsListAction
}

sealed interface ClubsListEvent {
    data class ShowMessage(val message: UiText) : ClubsListEvent
}
