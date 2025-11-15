package com.kikepb.auth.presentation.forgot_password

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import com.kikepb.domain.usecase.ForgotPasswordUseCase
import com.kikepb.domain.validation.EmailValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val isEmailValidFlow = snapshotFlow { state.value.emailTextFieldState.text.toString() }
        .map { email -> EmailValidator.validate(email = email) }
        .distinctUntilChanged()
    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeValidationState()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ForgotPasswordState()
        )

    private fun observeValidationState() {
        isEmailValidFlow.onEach { isEmailValid ->
            _state.update { it.copy(canSubmit = isEmailValid) }
        }.launchIn(scope =viewModelScope)
    }

    private fun submitForgotPasswordRequest() {
        if (state.value.isLoading || !state.value.canSubmit) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isEmailSentSuccessfully = false, errorText = null) }

            val email = state.value.emailTextFieldState.text.toString()

            forgotPasswordUseCase.forgotPassword(email = email)
                .onSuccess {
                    _state.update { it.copy(isEmailSentSuccessfully = true, isLoading = false) }

                }
                .onFailure { error ->
                    _state.update { it.copy(errorText = error.toUiText(), isLoading = false) }
                }
        }
    }

    fun onAction(action: ForgotPasswordAction) {
        when (action) {
            is ForgotPasswordAction.OnSubmitClick -> submitForgotPasswordRequest()
        }
    }
}

data class ForgotPasswordState(
    val emailTextFieldState: TextFieldState = TextFieldState(),
    val emailError: UiText? = null,
    val canSubmit: Boolean = false,
    val isLoading: Boolean = false,
    val errorText: UiText? = null,
    val isEmailSentSuccessfully: Boolean = false
)

sealed interface ForgotPasswordAction {
    data object OnSubmitClick: ForgotPasswordAction
}