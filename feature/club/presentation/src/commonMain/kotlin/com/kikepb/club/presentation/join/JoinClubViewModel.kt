package com.kikepb.club.presentation.join

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.model.JoinClubError.InvalidInvitationCodeFormat
import com.kikepb.club.domain.model.JoinClubError.InvalidShirtNumber
import com.kikepb.club.domain.model.JoinClubError.Remote
import com.kikepb.club.domain.usecase.JoinClubUseCase
import com.kikepb.club.presentation.join.JoinClubAction.OnClearErrors
import com.kikepb.club.presentation.join.JoinClubAction.OnJoinClub
import com.kikepb.club.presentation.mapper.toUiText
import com.kikepb.core.domain.util.Result.Failure
import com.kikepb.core.domain.util.Result.Success
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JoinClubViewModel(
    private val joinClubUseCase: JoinClubUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(JoinClubState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<JoinClubEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: JoinClubAction) {
        when (action) {
            OnJoinClub -> joinClub()
            OnClearErrors -> _state.update {
                it.copy(invitationCodeError = null, shirtNumberError = null)
            }
        }
    }

    private fun joinClub() {
        _state.update { it.copy(isLoading = true, invitationCodeError = null, shirtNumberError = null) }

        viewModelScope.launch {
            when (val result = joinClubUseCase(
                invitationCode = _state.value.invitationCodeState.text.toString().trim(),
                shirtNumber = _state.value.shirtNumberState.text.toString().trim().ifBlank { null },
                position = _state.value.positionState.text.toString().trim().ifBlank { null }
            )) {
                is Success -> {
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(JoinClubEvent.Success)
                }
                is Failure -> when (val error = result.error) {
                    InvalidInvitationCodeFormat -> _state.update { it.copy(isLoading = false, invitationCodeError = error.toUiText()) }
                    InvalidShirtNumber -> _state.update { it.copy(isLoading = false, shirtNumberError = error.toUiText()) }
                    is Remote -> {
                        _state.update { it.copy(isLoading = false) }
                        eventChannel.send(JoinClubEvent.Error(error.toUiText()))
                    }
                }
            }
        }
    }
}

data class JoinClubState(
    val invitationCodeState: TextFieldState = TextFieldState(),
    val shirtNumberState: TextFieldState = TextFieldState(),
    val positionState: TextFieldState = TextFieldState(),
    val isLoading: Boolean = false,
    val invitationCodeError: UiText? = null,
    val shirtNumberError: UiText? = null
) {
    val canSubmit: Boolean get() = invitationCodeState.text.isNotBlank() && !isLoading
}

sealed interface JoinClubAction {
    data object OnJoinClub : JoinClubAction
    data object OnClearErrors : JoinClubAction
}

sealed interface JoinClubEvent {
    data object Success : JoinClubEvent
    data class Error(val message: UiText) : JoinClubEvent
}
