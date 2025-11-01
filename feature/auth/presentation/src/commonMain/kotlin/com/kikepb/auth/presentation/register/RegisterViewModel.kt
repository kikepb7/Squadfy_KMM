package com.kikepb.auth.presentation.register

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.domain.validation.PasswordValidator
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import com.kikepb.domain.usecase.AuthRegisterUseCase
import com.kikepb.domain.validation.EmailValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import squadfy_app.feature.auth.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.auth.presentation.generated.resources.error_account_exists
import squadfy_app.feature.auth.presentation.generated.resources.error_invalid_email
import squadfy_app.feature.auth.presentation.generated.resources.error_invalid_password
import squadfy_app.feature.auth.presentation.generated.resources.error_invalid_username

class RegisterViewModel(
    private val authRegisterUseCase: AuthRegisterUseCase
) : ViewModel() {

    private val eventChannel = Channel<RegisterEvent>()
    val events = eventChannel.receiveAsFlow()
    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(RegisterState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
            initialValue = RegisterState()
        )

    private fun validateFormInputs(): Boolean {
        clearAllTextFieldErrors()

        val currentState = state.value
        val email = currentState.emailTextState.text.toString()
        val username = currentState.usernameTextState.text.toString()
        val password = currentState.passwordTextState.text.toString()

        val isEmailValid = EmailValidator.validate(email = email)
        val passwordValidationState = PasswordValidator.validate(password = password)
        val isUsernameValid = username.length in 3..20

        val usernameError = if (!isUsernameValid) UiText.Resource(RString.error_invalid_username) else null
        val emailError = if (!isEmailValid) UiText.Resource(RString.error_invalid_email) else null
        val passwordError = if (!passwordValidationState.isValidPassword) UiText.Resource(RString.error_invalid_password) else null

        _state.update {
            it.copy(usernameError = usernameError, emailError = emailError, passwordError = passwordError)
        }

        return isUsernameValid && isEmailValid && passwordValidationState.isValidPassword
    }

    private fun clearAllTextFieldErrors() {
        _state.update {
            it.copy(usernameError = null, emailError = null, passwordError = null, registrationError = null)
        }
    }

    private fun register() {
        if (!validateFormInputs()) return

        viewModelScope.launch {
            _state.update { it.copy(isRegistering = true) }

            val username = state.value.usernameTextState.text.toString()
            val email = state.value.emailTextState.text.toString()
            val password = state.value.passwordTextState.text.toString()

            authRegisterUseCase.authRegister(username = username, email = email, password = password)
                .onSuccess {
                    _state.update { it.copy(isRegistering = false) }
                }
                .onFailure { error ->
                    val registrationError = when (error) {
                        DataError.Remote.CONFLICT -> UiText.Resource(RString.error_account_exists)
                        else -> error.toUiText()
                    }
                    _state.update {
                        it.copy(
                            isRegistering = false,
                            registrationError = registrationError
                        )
                    }
                }
        }
    }

    fun onAction(action: RegisterAction) {
        when (action) {
            RegisterAction.OnLoginClick -> Unit
            RegisterAction.OnRegisterClick -> register()
            RegisterAction.OnTogglePasswordVisibilityClick -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            else -> Unit
        }
    }
}

data class RegisterState(
    val emailTextState: TextFieldState = TextFieldState(),
    val isEmailValid: Boolean = false,
    val emailError: UiText? = null,
    val passwordTextState: TextFieldState = TextFieldState(),
    val isPasswordValid: Boolean = false,
    val passwordError: UiText? = null,
    val usernameTextState: TextFieldState = TextFieldState(),
    val isUsernameValid: Boolean = false,
    val usernameError: UiText? = null,
    val registrationError: UiText? = null,
    val isRegistering: Boolean = false,
    val canRegister: Boolean = false,
    val isPasswordVisible: Boolean = false
)

sealed interface RegisterEvent {
    data class Success(val email: String): RegisterEvent
}

sealed interface RegisterAction {
    data object OnLoginClick: RegisterAction
    data object OnInputTextFocusGain: RegisterAction
    data object OnRegisterClick: RegisterAction
    data object OnTogglePasswordVisibilityClick: RegisterAction
}