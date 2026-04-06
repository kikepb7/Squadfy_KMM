package com.kikepb.club.presentation.join

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.club.domain.usecase.JoinClubUseCase
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

class JoinClubViewModel(
    private val joinClubUseCase: JoinClubUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(JoinClubState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<JoinClubEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: JoinClubAction) {
        when (action) {
            JoinClubAction.OnSubmit -> submit()
            JoinClubAction.OnClearErrors -> _state.update {
                it.copy(invitationCodeError = null, shirtNumberError = null)
            }
        }
    }

    private fun submit() {
        val code = _state.value.invitationCodeState.text.toString().trim()
        val shirtNumberStr = _state.value.shirtNumberState.text.toString().trim()
        val position = _state.value.positionState.text.toString().trim().ifBlank { null }

        if (code.isBlank()) {
            _state.update { it.copy(invitationCodeError = "El código de invitación es obligatorio") }
            return
        }
        if (!code.matches(Regex("^[A-Za-z0-9]{6,12}$"))) {
            _state.update { it.copy(invitationCodeError = "Debe tener entre 6 y 12 caracteres alfanuméricos") }
            return
        }

        val shirtNumber: Int? = if (shirtNumberStr.isBlank()) {
            null
        } else {
            val n = shirtNumberStr.toIntOrNull()
            if (n == null) {
                _state.update { it.copy(shirtNumberError = "Introduce un número válido") }
                return
            }
            if (n !in 1..100) {
                _state.update { it.copy(shirtNumberError = "Debe estar entre 1 y 100") }
                return
            }
            n
        }

        _state.update { it.copy(isLoading = true, invitationCodeError = null, shirtNumberError = null) }

        viewModelScope.launch {
            joinClubUseCase(
                invitationCode = code,
                shirtNumber = shirtNumber,
                position = position
            )
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(JoinClubEvent.Success)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(JoinClubEvent.Error(error.toUiText()))
                }
        }
    }
}

data class JoinClubState(
    val invitationCodeState: TextFieldState = TextFieldState(),
    val shirtNumberState: TextFieldState = TextFieldState(),
    val positionState: TextFieldState = TextFieldState(),
    val isLoading: Boolean = false,
    val invitationCodeError: String? = null,
    val shirtNumberError: String? = null
) {
    val canSubmit: Boolean get() = invitationCodeState.text.isNotBlank() && !isLoading
}

sealed interface JoinClubAction {
    data object OnSubmit : JoinClubAction
    data object OnClearErrors : JoinClubAction
}

sealed interface JoinClubEvent {
    data object Success : JoinClubEvent
    data class Error(val message: UiText) : JoinClubEvent
}
