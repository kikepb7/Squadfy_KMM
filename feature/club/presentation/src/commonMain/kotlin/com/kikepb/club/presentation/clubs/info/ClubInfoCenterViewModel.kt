package com.kikepb.club.presentation.clubs.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.usecase.CreateClubUseCase
import com.kikepb.club.domain.usecase.JoinClubUseCase
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

class ClubInfoCenterViewModel(
    private val createClubUseCase: CreateClubUseCase,
    private val joinClubUseCase: JoinClubUseCase,
    private val clubRefreshNotifier: ClubRefreshNotifier
) : ViewModel() {

    private val eventChannel = Channel<ClubInfoCenterEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ClubInfoCenterState())
    val state = _state.asStateFlow()

    fun onAction(action: ClubInfoCenterAction) {
        when (action) {
            is ClubInfoCenterAction.OnClubNameChange -> _state.update { it.copy(newClubName = action.value) }
            is ClubInfoCenterAction.OnClubDescriptionChange -> _state.update { it.copy(newClubDescription = action.value) }
            is ClubInfoCenterAction.OnClubLogoUrlChange -> _state.update { it.copy(newClubLogoUrl = action.value) }
            is ClubInfoCenterAction.OnClubMaxMembersChange -> _state.update { it.copy(newClubMaxMembers = action.value) }
            is ClubInfoCenterAction.OnInvitationCodeChange -> _state.update { it.copy(invitationCode = action.value) }
            is ClubInfoCenterAction.OnShirtNumberChange -> _state.update { it.copy(shirtNumber = action.value) }
            is ClubInfoCenterAction.OnPositionChange -> _state.update { it.copy(position = action.value) }
            ClubInfoCenterAction.OnCreateClubClick -> createClub()
            ClubInfoCenterAction.OnJoinClubClick -> joinClub()
        }
    }

    private fun createClub() {
        val currentState = state.value
        val name = currentState.newClubName.trim()

        if (name.isBlank()) {
            sendMessage("El nombre del equipo es obligatorio")
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            createClubUseCase
                .createClub(
                    name = name,
                    description = currentState.newClubDescription.trim().ifBlank { null },
                    clubLogoUrl = currentState.newClubLogoUrl.trim().ifBlank { null },
                    maxMembers = currentState.newClubMaxMembers.toIntOrNull()
                )
                .onSuccess {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            newClubName = "",
                            newClubDescription = "",
                            newClubLogoUrl = "",
                            newClubMaxMembers = ""
                        )
                    }
                    clubRefreshNotifier.requestRefresh()
                    eventChannel.send(ClubInfoCenterEvent.ShowMessage(UiText.DynamicString("Equipo creado correctamente")))
                    eventChannel.send(ClubInfoCenterEvent.NavigateToTeams)
                }
                .onFailure { error ->
                    _state.update { it.copy(isSubmitting = false) }
                    eventChannel.send(ClubInfoCenterEvent.ShowMessage(error.toUiText()))
                }
        }
    }

    private fun joinClub() {
        val currentState = state.value
        val code = currentState.invitationCode.trim()

        if (code.isBlank()) {
            sendMessage("El código de invitación es obligatorio")
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            joinClubUseCase
                .joinClub(
                    invitationCode = code,
                    shirtNumber = currentState.shirtNumber.toIntOrNull(),
                    position = currentState.position.trim().ifBlank { null }
                )
                .onSuccess {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            invitationCode = "",
                            shirtNumber = "",
                            position = ""
                        )
                    }
                    clubRefreshNotifier.requestRefresh()
                    eventChannel.send(ClubInfoCenterEvent.ShowMessage(UiText.DynamicString("Te has unido al equipo")))
                    eventChannel.send(ClubInfoCenterEvent.NavigateToTeams)
                }
                .onFailure { error ->
                    _state.update { it.copy(isSubmitting = false) }
                    eventChannel.send(ClubInfoCenterEvent.ShowMessage(error.toUiText()))
                }
        }
    }

    private fun sendMessage(message: String) {
        viewModelScope.launch {
            eventChannel.send(ClubInfoCenterEvent.ShowMessage(UiText.DynamicString(message)))
        }
    }
}

data class ClubInfoCenterState(
    val isSubmitting: Boolean = false,
    val newClubName: String = "",
    val newClubDescription: String = "",
    val newClubLogoUrl: String = "",
    val newClubMaxMembers: String = "",
    val invitationCode: String = "",
    val shirtNumber: String = "",
    val position: String = ""
)

sealed interface ClubInfoCenterAction {
    data class OnClubNameChange(val value: String) : ClubInfoCenterAction
    data class OnClubDescriptionChange(val value: String) : ClubInfoCenterAction
    data class OnClubLogoUrlChange(val value: String) : ClubInfoCenterAction
    data class OnClubMaxMembersChange(val value: String) : ClubInfoCenterAction
    data class OnInvitationCodeChange(val value: String) : ClubInfoCenterAction
    data class OnShirtNumberChange(val value: String) : ClubInfoCenterAction
    data class OnPositionChange(val value: String) : ClubInfoCenterAction
    data object OnCreateClubClick : ClubInfoCenterAction
    data object OnJoinClubClick : ClubInfoCenterAction
}

sealed interface ClubInfoCenterEvent {
    data class ShowMessage(val message: UiText) : ClubInfoCenterEvent
    data object NavigateToTeams : ClubInfoCenterEvent
}
