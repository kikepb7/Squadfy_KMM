package org.kikepb.squadfy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.core.domain.auth.repository.SessionStorage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kikepb.squadfy.MainEvent.OnSessionExpired

class MainViewModel(
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private var previousRefreshToken: String? = null
    private var hasLoadedInitialData = false
    private val eventChannel = Channel<MainEvent>()
    val events = eventChannel.receiveAsFlow()
    private val _state = MutableStateFlow(MainState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeSession()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MainState()
        )


    init {
        viewModelScope.launch {
            val authInfo = sessionStorage.observeAuthInfo().firstOrNull()

            _state.update { it.copy(isCheckingAuth = false, isLoggedIn = authInfo != null) }
        }
    }

    private fun observeSession() {
        sessionStorage
            .observeAuthInfo()
            .onEach { authInfo ->
                val currentRefreshToken = authInfo?.refreshToken
                val isSessionExpired = previousRefreshToken != null && currentRefreshToken == null

                if (isSessionExpired) {
                    sessionStorage.set(info = null)
                    _state.update { it.copy(isLoggedIn = false) }
                    eventChannel.send(OnSessionExpired)
                }

                previousRefreshToken = authInfo?.refreshToken
            }
            .launchIn(viewModelScope)
    }
}

data class MainState(
    val isLoggedIn: Boolean = false,
    val isCheckingAuth: Boolean = true
)

sealed interface MainEvent {
    data object OnSessionExpired: MainEvent
}