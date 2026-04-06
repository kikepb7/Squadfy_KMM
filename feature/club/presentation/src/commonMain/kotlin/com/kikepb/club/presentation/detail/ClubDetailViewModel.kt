package com.kikepb.club.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.usecase.FetchClubByIdUseCase
import com.kikepb.club.domain.usecase.FetchClubMembersUseCase
import com.kikepb.club.domain.usecase.GetClubByIdUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.presentation.detail.ClubDetailEvent.ShowMessage
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.presentation.mapper.toUiText
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

class ClubDetailViewModel(
    private val getClubByIdUseCase: GetClubByIdUseCase,
    private val getClubMembersUseCase: GetClubMembersUseCase,
    private val fetchClubByIdUseCase: FetchClubByIdUseCase,
    private val fetchClubMembersUseCase: FetchClubMembersUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")

    private val eventChannel = Channel<ClubDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ClubDetailState())

    val state = combine(
        _state,
        getClubByIdUseCase(clubId = clubId),
        getClubMembersUseCase(clubId = clubId)
    ) { current, club, members ->
        current.copy(club = club, members = members, isLoading = false)
    }
        .onStart { syncFromNetwork() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
            initialValue = ClubDetailState()
        )

    fun onAction(action: ClubDetailAction) {
        when (action) {
            ClubDetailAction.OnRefresh -> syncFromNetwork()
        }
    }

    private fun syncFromNetwork() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            fetchClubByIdUseCase(clubId = clubId)
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(element = ShowMessage(error.toUiText()))
                }
        }
        viewModelScope.launch {
            fetchClubMembersUseCase(clubId = clubId)
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(element = ShowMessage(error.toUiText()))
                }
        }
    }
}

data class ClubDetailState(
    val isLoading: Boolean = true,
    val club: ClubModel? = null,
    val members: List<ClubMemberModel> = emptyList()
)

sealed interface ClubDetailAction {
    data object OnRefresh : ClubDetailAction
}

sealed interface ClubDetailEvent {
    data class ShowMessage(val message: UiText) : ClubDetailEvent
}
