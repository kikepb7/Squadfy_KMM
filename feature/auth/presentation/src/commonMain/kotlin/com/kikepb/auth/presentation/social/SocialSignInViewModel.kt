package com.kikepb.auth.presentation.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.core.domain.auth.model.SignInMethod
import com.kikepb.core.domain.auth.model.SocialCredentialResult
import com.kikepb.core.domain.auth.model.SocialSignInResult
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.auth.repository.SocialCredentialProvider
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.util.UiText
import com.kikepb.core.presentation.mapper.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import squadfy_app.feature.auth.presentation.generated.resources.Res
import squadfy_app.feature.auth.presentation.generated.resources.social_sign_in_failed
import squadfy_app.feature.auth.presentation.generated.resources.social_sign_in_unavailable

data class SocialSignInState(
    val methods: List<SignInMethod> = emptyList(),
    /** The method being signed in with; the buttons are disabled meanwhile. */
    val inProgress: SignInMethod? = null,
    val error: UiText? = null
)

sealed interface SocialSignInAction {
    data class OnMethodClick(val method: SignInMethod) : SocialSignInAction
}

sealed interface SocialSignInEvent {
    data object SignedIn : SocialSignInEvent
    data class NeedsProfile(val suggestedUsername: String) : SocialSignInEvent
}

/**
 * Google and Apple sign-in (spec 019 AC-019-23), shared by the welcome and login screens: the platform sheet gives a
 * credential, the account signs in with it, and the first time the username is still to be chosen.
 */
class SocialSignInViewModel(
    private val provider: SocialCredentialProvider,
    private val authRepository: AuthRepository,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _state = MutableStateFlow(SocialSignInState(methods = provider.availableMethods))
    val state = _state.asStateFlow()

    private val eventChannel = Channel<SocialSignInEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: SocialSignInAction) {
        when (action) {
            is SocialSignInAction.OnMethodClick -> signIn(action.method)
        }
    }

    private fun signIn(method: SignInMethod) {
        if (_state.value.inProgress != null) return
        viewModelScope.launch {
            _state.update { it.copy(inProgress = method, error = null) }
            when (val credential = provider.obtainCredential(method)) {
                is SocialCredentialResult.Success -> authRepository.signInWithProvider(credential.credential)
                    .onSuccess { result ->
                        when (result) {
                            is SocialSignInResult.SignedIn -> {
                                sessionStorage.set(result.authInfo)
                                eventChannel.send(SocialSignInEvent.SignedIn)
                            }
                            is SocialSignInResult.NeedsProfile -> eventChannel.send(SocialSignInEvent.NeedsProfile(result.suggestedUsername))
                        }
                    }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
                SocialCredentialResult.Cancelled -> Unit
                SocialCredentialResult.Unavailable -> _state.update { it.copy(error = UiText.Resource(Res.string.social_sign_in_unavailable)) }
                SocialCredentialResult.Failed -> _state.update { it.copy(error = UiText.Resource(Res.string.social_sign_in_failed)) }
            }
            _state.update { it.copy(inProgress = null) }
        }
    }
}
