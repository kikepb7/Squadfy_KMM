package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.notification.PushNotificationService
import com.kikepb.core.domain.auth.repository.SessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Full sign-out, shared by the chat list menu and Profile (spec 015, D-13: Profile must work without the chat).
 * 1. Spec 009 (AC-009-01): `DELETE /devices/{fcmToken}` needs the session, so it goes first, bounded so that
 *    signing out never hangs without internet.
 * 2. Local sign-out always succeeds: clearing the session makes the app unauthenticated immediately.
 * 3. Revoking the refresh token is best effort on [applicationScope], so leaving the screen never cancels it;
 *    offline, the token simply expires on the server.
 */
class SignOutUseCase(
    private val sessionStorage: SessionStorage,
    private val pushNotificationService: PushNotificationService,
    private val unregisterTokenUseCase: UnregisterTokenUseCase,
    private val deleteAllChatsUseCase: DeleteAllChatsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val applicationScope: CoroutineScope
) {
    suspend operator fun invoke() {
        val refreshToken = sessionStorage.observeAuthInfo().first()?.refreshToken

        withTimeoutOrNull(DEVICE_UNREGISTER_TIMEOUT_MS) {
            pushNotificationService.observeDeviceToken().firstOrNull()?.let { unregisterTokenUseCase.unregisterToken(token = it) }
        }

        sessionStorage.set(info = null)
        deleteAllChatsUseCase.deleteAllChats()

        if (refreshToken != null) {
            applicationScope.launch { logoutUseCase.logout(refreshToken = refreshToken) }
        }
    }

    private companion object {
        const val DEVICE_UNREGISTER_TIMEOUT_MS = 3_000L
    }
}
