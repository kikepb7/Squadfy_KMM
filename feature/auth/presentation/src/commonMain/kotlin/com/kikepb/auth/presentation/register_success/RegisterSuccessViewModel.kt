package com.kikepb.auth.presentation.register_success

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.auth.presentation.register_success.RegisterSuccessEvent.ResentVerificationEmailSuccess
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import com.kikepb.domain.usecase.ResendEmailVerificationUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterSuccessViewModel(
    private val resendEmailVerificationUseCase: ResendEmailVerificationUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val eventChannel = Channel<RegisterSuccessEvent>()
    val events = eventChannel.receiveAsFlow()
    private val email = savedStateHandle.get<String>("email")
        ?: throw IllegalStateException("No email passed to register success screen")
    private val _state = MutableStateFlow(RegisterSuccessState(registeredEmail = email ?: ""))
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) hasLoadedInitialData = true
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RegisterSuccessState()
        )

    private fun resendVerificationAction() {
        if (state.value.isResendingVerificationEmail) return

        viewModelScope.launch {
            _state.update { it.copy(isResendingVerificationEmail = true) }

            resendEmailVerificationUseCase.invoke(email = email)
                .onSuccess {
                    _state.update { it.copy(isResendingVerificationEmail = false) }
                    eventChannel.send(element = ResentVerificationEmailSuccess)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isResendingVerificationEmail = false, resendVerificationError = error.toUiText())
                    }
                }
        }
    }

    fun onAction(action: RegisterSuccessAction) {
        when (action) {
            is RegisterSuccessAction.OnResendVerificationEmailClick -> resendVerificationAction()
            else -> Unit
        }
    }
}

data class RegisterSuccessState(
    val registeredEmail: String = "",
    val isResendingVerificationEmail: Boolean = false,
    val resendVerificationError: UiText? = null
)

sealed interface RegisterSuccessEvent {
    data object ResentVerificationEmailSuccess: RegisterSuccessEvent
}

sealed interface RegisterSuccessAction {
    data object OnLoginClick: RegisterSuccessAction
    data object OnResendVerificationEmailClick: RegisterSuccessAction
}
