package com.kikepb.chat.presentation.fake

import com.kikepb.chat.domain.notification.PushNotificationService
import com.kikepb.chat.domain.usecases.DeleteAllChatsUseCase
import com.kikepb.chat.domain.usecases.LogoutUseCase
import com.kikepb.chat.domain.usecases.SignOutUseCase
import com.kikepb.chat.domain.usecases.UnregisterTokenUseCase
import com.kikepb.core.domain.auth.repository.SessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Sign-out wired to fakes; the device token flow is empty so nothing waits for push. */
fun fakeSignOutUseCase(
    sessionStorage: SessionStorage,
    chatRepository: FakeChatRepository = FakeChatRepository(),
    authRepository: FakeAuthRepository = FakeAuthRepository(),
    applicationScope: CoroutineScope
) = SignOutUseCase(
    sessionStorage = sessionStorage,
    pushNotificationService = object : PushNotificationService {
        override fun observeDeviceToken(): Flow<String?> = flowOf(null)
    },
    unregisterTokenUseCase = UnregisterTokenUseCase(deviceTokenService = FakeDeviceTokenService()),
    deleteAllChatsUseCase = DeleteAllChatsUseCase(chatRepository = chatRepository),
    logoutUseCase = LogoutUseCase(authRepository = authRepository),
    applicationScope = applicationScope
)
