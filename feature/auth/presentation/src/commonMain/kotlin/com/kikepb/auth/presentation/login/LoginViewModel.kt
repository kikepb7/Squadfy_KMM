package com.kikepb.auth.presentation.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import com.kikepb.domain.usecase.LoginUseCase
import com.kikepb.domain.validation.EmailValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import squadfy_app.feature.auth.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.auth.presentation.generated.resources.error_email_not_verified
import squadfy_app.feature.auth.presentation.generated.resources.error_invalid_credentials

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val isEmailValidFlow = snapshotFlow { state.value.emailTextFieldState.text.toString() }
        .map { email -> EmailValidator.validate(email) }
        .distinctUntilChanged()
    private val isPasswordNotBlankFlow = snapshotFlow { state.value.passwordTextFieldState.text.toString() }
        .map { it.isNotBlank() }
        .distinctUntilChanged()

    private val eventChannel = Channel<LoginEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(LoginState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeTextStates()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = LoginState()
        )

    private val isRegisteringFlow = state
        .map { it.isLoggingIn }
        .distinctUntilChanged()

    private fun login() {
        if (!state.value.canLogin) return

        viewModelScope.launch {
            _state.update { it.copy(isLoggingIn = true) }

            val email = state.value.emailTextFieldState.text.toString()
            val password = state.value.passwordTextFieldState.text.toString()

            loginUseCase.login(email = email, password = password)
                .onSuccess { authInfoModel ->
                    sessionStorage.set(info = authInfoModel)

                    _state.update { it.copy(isLoggingIn = false) }
                    eventChannel.send(LoginEvent.Success)
                }
                .onFailure { error ->
                    val errorMessage = when (error) {
                        DataError.Remote.UNAUTHORIZED -> UiText.Resource(RString.error_invalid_credentials)
                        DataError.Remote.FORBIDDEN -> UiText.Resource(RString.error_email_not_verified)
                        else -> error.toUiText()
                    }

                    _state.update { it.copy(error = errorMessage, isLoggingIn = false) }
                }
        }
    }

    private fun observeTextStates() {
        combine(
            isEmailValidFlow,
            isPasswordNotBlankFlow,
            isRegisteringFlow
        ) { isEmailValid, isPasswordNotBlank, isRegistering ->
            _state.update { it.copy(
                canLogin = !isRegistering && isEmailValid && isPasswordNotBlank
            ) }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.OnLoginClick -> login()
            LoginAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            else -> Unit
        }
    }
}

data class LoginState(
    val emailTextFieldState: TextFieldState = TextFieldState(),
    val passwordTextFieldState: TextFieldState = TextFieldState(),
    val isPasswordVisible: Boolean = false,
    val canLogin: Boolean = false,
    val isLoggingIn: Boolean = false,
    val error: UiText? = null
)

sealed interface LoginEvent {
    data object Success: LoginEvent
}

sealed interface LoginAction {
    data object OnTogglePasswordVisibility: LoginAction
    data object OnForgotPasswordClick: LoginAction
    data object OnLoginClick: LoginAction
    data object OnSignUpClick: LoginAction
}