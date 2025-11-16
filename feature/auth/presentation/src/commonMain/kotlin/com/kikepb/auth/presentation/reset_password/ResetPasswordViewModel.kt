package com.kikepb.auth.presentation.reset_password

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.domain.validation.PasswordValidator
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import com.kikepb.domain.usecase.ResetPasswordUseCase
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
import squadfy_app.feature.auth.presentation.generated.resources.error_reset_password_token_invalid
import squadfy_app.feature.auth.presentation.generated.resources.error_same_password
import squadfy_app.feature.auth.presentation.generated.resources.Res.string as RString

class ResetPasswordViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    savedStateHandler: SavedStateHandle
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val token = savedStateHandler.get<String>("token")
        ?: throw IllegalStateException("No password reset token")
    private val isPasswordValidFlow = snapshotFlow { state.value.passwordTextState.text.toString() }
        .map { password -> PasswordValidator.validate(password = password).isValidPassword }
        .distinctUntilChanged()

    private val _state = MutableStateFlow(ResetPasswordState())
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
            initialValue = ResetPasswordState()
        )

    private fun observeValidationState() {
        isPasswordValidFlow.onEach { isPasswordValid ->
            _state.update { it.copy(canSubmit = isPasswordValid) }
        }.launchIn(scope = viewModelScope)
    }

    private fun resetPassword() {
        if (state.value.isLoading || !state.value.canSubmit) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isResetSuccessful = false) }

            val newPassword = state.value.passwordTextState.text.toString()
            resetPasswordUseCase.resetPassword(newPassword = newPassword, token = token)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isResetSuccessful = true, errorText = null) }
                }
                .onFailure { error ->
                    val errorText = when (error) {
                        DataError.Remote.UNAUTHORIZED -> UiText.Resource(RString.error_reset_password_token_invalid)
                        DataError.Remote.CONFLICT -> UiText.Resource(RString.error_same_password)
                        else -> error.toUiText()
                    }

                    _state.update { it.copy(errorText = errorText, isLoading = false) }
                }
        }
    }

    fun onAction(action: ResetPasswordAction) {
        when (action) {
            ResetPasswordAction.OnSubmitClick -> resetPassword()
            ResetPasswordAction.OnTogglePasswordVisibilityClick -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
        }
    }
}

data class ResetPasswordState(
    val passwordTextState: TextFieldState = TextFieldState(),
    val isLoading: Boolean = false,
    val errorText: UiText? = null,
    val isPasswordVisible: Boolean = false,
    val canSubmit: Boolean = false,
    val isResetSuccessful: Boolean = false
)

sealed interface ResetPasswordAction {
    data object OnSubmitClick: ResetPasswordAction
    data object OnTogglePasswordVisibilityClick: ResetPasswordAction
}