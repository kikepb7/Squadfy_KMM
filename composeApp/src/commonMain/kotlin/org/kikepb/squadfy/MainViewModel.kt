package org.kikepb.squadfy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.chat.domain.notification.DeviceTokenService
import com.kikepb.chat.domain.notification.PushNotificationService
import com.kikepb.core.data.util.PlatformUtils
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.onboarding.domain.usecase.GetHasSeenOnboardingUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kikepb.squadfy.MainEvent.OnSessionExpired
import org.kikepb.squadfy.StartDestination.Auth
import org.kikepb.squadfy.StartDestination.Home
import org.kikepb.squadfy.StartDestination.Onboarding

class MainViewModel(
    private val sessionStorage: SessionStorage,
    private val pushNotificationService: PushNotificationService,
    private val deviceTokenService: DeviceTokenService,
    private val getHasSeenOnboardingUseCase: GetHasSeenOnboardingUseCase
) : ViewModel() {

    private var previousRefreshToken: String? = null
    private var currentDeviceToken: String? = null
    private var previousDeviceToken: String? = null
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
            val hasSeenOnboarding = getHasSeenOnboardingUseCase().firstOrNull() ?: false
            _state.update {
                it.copy(
                    isCheckingAuth = false,
                    isLoggedIn = authInfo != null,
                    hasSeenOnboarding = hasSeenOnboarding
                )
            }
        }

        getHasSeenOnboardingUseCase()
            .onEach { hasSeenOnboarding ->
                _state.update { it.copy(hasSeenOnboarding = hasSeenOnboarding) }
            }
            .launchIn(viewModelScope)
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
                    currentDeviceToken?.let {
                        deviceTokenService.unregisterToken(token = it)
                    }
                    eventChannel.send(OnSessionExpired)
                }

                previousRefreshToken = authInfo?.refreshToken
            }
            .combine(flow = pushNotificationService.observeDeviceToken()) { authInfo, deviceToken ->
                currentDeviceToken = deviceToken
                if (authInfo != null && deviceToken != previousDeviceToken && deviceToken != null) {
                    registerDeviceToken(token = deviceToken, platform = PlatformUtils.getOSName())
                    previousDeviceToken = deviceToken
                }
            }
            .launchIn(scope = viewModelScope)
    }

    fun onLoginCompleted() {
        viewModelScope.launch {
            val hasSeenOnboarding = getHasSeenOnboardingUseCase().firstOrNull() ?: false
            val destination = if (hasSeenOnboarding) Home else Onboarding
            eventChannel.send(MainEvent.NavigateAfterLogin(destination = destination))
        }
    }

    private fun registerDeviceToken(token: String, platform: String) {
        viewModelScope.launch {
            deviceTokenService.registerToken(token = token, platform = platform)
        }
    }
}

data class MainState(
    val isLoggedIn: Boolean = false,
    val isCheckingAuth: Boolean = true,
    val hasSeenOnboarding: Boolean = false
) {
    val startDestination: StartDestination
        get() = when {
            !isLoggedIn -> Auth
            !hasSeenOnboarding -> Onboarding
            else -> Home
        }
}

sealed interface StartDestination {
    data object Auth : StartDestination
    data object Onboarding : StartDestination
    data object Home : StartDestination
}

sealed interface MainEvent {
    data object OnSessionExpired : MainEvent
    data class NavigateAfterLogin(val destination: StartDestination) : MainEvent
}