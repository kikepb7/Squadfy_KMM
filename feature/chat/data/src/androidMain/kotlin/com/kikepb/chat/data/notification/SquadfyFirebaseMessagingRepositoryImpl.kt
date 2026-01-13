package com.kikepb.chat.data.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.kikepb.chat.domain.notification.DeviceTokenService
import com.kikepb.core.domain.auth.repository.SessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SquadfyFirebaseMessagingRepositoryImpl: FirebaseMessagingService() {

    private val deviceTokenService by inject<DeviceTokenService>()
    private val sessionStorage by inject<SessionStorage>()
    private val applicationScope by inject<CoroutineScope>()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        applicationScope.launch {
            val authInfo = sessionStorage.observeAuthInfo().first()

            if (authInfo != null) deviceTokenService.registerToken(token = token, platform = "ANDROID")
        }
    }
}