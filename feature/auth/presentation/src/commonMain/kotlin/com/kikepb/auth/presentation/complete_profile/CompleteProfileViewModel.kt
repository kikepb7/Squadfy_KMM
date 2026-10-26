package com.kikepb.auth.presentation.complete_profile

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.util.UiText
import com.kikepb.core.presentation.mapper.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import squadfy_app.feature.auth.presentation.generated.resources.Res
import squadfy_app.feature.auth.presentation.generated.resources.error_username_taken

data class CompleteProfileState(
    val usernameTextState: TextFieldState = TextFieldState(),
    val canContinue: Boolean = false,
    val isSaving: Boolean = false,
    val error: UiText? = null
)

sealed interface CompleteProfileAction {
    data object OnContinueClick : CompleteProfileAction
    data object OnCancelClick : CompleteProfileAction
}

sealed interface CompleteProfileEvent {
    data object Done : CompleteProfileEvent
    data object Left : CompleteProfileEvent
}

/**
 * The username step of a first Google/Apple sign-in (spec 019 AC-019-23): 3–20 characters and unique, as when
 * registering with email. Leaving it deletes the half-created account.
 */
class CompleteProfileViewModel(
    suggestedUsername: String,
    private val authRepository: AuthRepository,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _state = MutableStateFlow(CompleteProfileState(usernameTextState = TextFieldState(suggestedUsername)))
    val state = _state.asStateFlow()

    private val eventChannel = Channel<CompleteProfileEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        snapshotFlow { _state.value.usernameTextState.text.toString().trim() }
            .onEach { username -> _state.update { it.copy(canContinue = username.length in 3..20, error = null) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: CompleteProfileAction) {
        when (action) {
            CompleteProfileAction.OnContinueClick -> save()
            CompleteProfileAction.OnCancelClick -> leave()
        }
    }

    private fun save() {
        val username = _state.value.usernameTextState.text.toString().trim()
        if (username.length !in 3..20 || _state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            authRepository.completeProfile(username)
                .onSuccess { authInfo ->
                    sessionStorage.set(authInfo)
                    eventChannel.send(CompleteProfileEvent.Done)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = if (error == DataError.Remote.CONFLICT) UiText.Resource(Res.string.error_username_taken) else error.toUiText())
                    }
                }
            _state.update { it.copy(isSaving = false) }
        }
    }

    private fun leave() {
        viewModelScope.launch {
            authRepository.abandonIncompleteAccount()
            eventChannel.send(CompleteProfileEvent.Left)
        }
    }
}
