package com.kikepb.club.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.usecase.GetClubByIdUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
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

class ClubDetailViewModel(
    private val getClubByIdUseCase: GetClubByIdUseCase,
    private val getClubMembersUseCase: GetClubMembersUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")

    private val eventChannel = Channel<ClubDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ClubDetailState())
    val state = _state.asStateFlow()

    init {
        loadClubData()
    }

    fun onAction(action: ClubDetailAction) {
        when (action) {
            ClubDetailAction.OnRefresh -> loadClubData()
        }
    }

    private fun loadClubData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            getClubByIdUseCase
                .getClubById(clubId = clubId)
                .onSuccess { club ->
                    _state.update { it.copy(club = club) }
                }
                .onFailure { error ->
                    eventChannel.send(ClubDetailEvent.ShowMessage(error.toUiText()))
                }

            getClubMembersUseCase
                .getClubMembers(clubId = clubId)
                .onSuccess { members ->
                    _state.update { it.copy(isLoading = false, members = members) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(ClubDetailEvent.ShowMessage(error.toUiText()))
                }
        }
    }
}

data class ClubDetailState(
    val isLoading: Boolean = false,
    val club: ClubModel? = null,
    val members: List<ClubMemberModel> = emptyList()
)

sealed interface ClubDetailAction {
    data object OnRefresh : ClubDetailAction
}

sealed interface ClubDetailEvent {
    data class ShowMessage(val message: UiText) : ClubDetailEvent
}