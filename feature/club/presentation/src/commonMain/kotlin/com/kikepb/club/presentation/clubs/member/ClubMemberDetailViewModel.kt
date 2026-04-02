package com.kikepb.club.presentation.clubs.member

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.ClubMemberModel
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

class ClubMemberDetailViewModel(
    private val getClubMembersUseCase: GetClubMembersUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val clubId = savedStateHandle.get<String>("clubId")
        ?: throw IllegalStateException("clubId is required")
    private val memberId = savedStateHandle.get<String>("memberId")
        ?: throw IllegalStateException("memberId is required")

    private val eventChannel = Channel<ClubMemberDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ClubMemberDetailState())
    val state = _state.asStateFlow()

    init {
        loadMember()
    }

    fun onAction(action: ClubMemberDetailAction) {
        when (action) {
            ClubMemberDetailAction.OnRefresh -> loadMember()
        }
    }

    private fun loadMember() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            getClubMembersUseCase
                .getClubMembers(clubId = clubId)
                .onSuccess { members ->
                    val member = members.firstOrNull { it.id == memberId }
                    _state.update { it.copy(isLoading = false, member = member) }

                    if (member == null) {
                        eventChannel.send(
                            ClubMemberDetailEvent.ShowMessage(
                                UiText.DynamicString("No se ha encontrado el integrante seleccionado")
                            )
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(ClubMemberDetailEvent.ShowMessage(error.toUiText()))
                }
        }
    }
}

data class ClubMemberDetailState(
    val isLoading: Boolean = false,
    val member: ClubMemberModel? = null
)

sealed interface ClubMemberDetailAction {
    data object OnRefresh : ClubMemberDetailAction
}

sealed interface ClubMemberDetailEvent {
    data class ShowMessage(val message: UiText) : ClubMemberDetailEvent
}
