package org.kikepb.squadfy.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kikepb.chat.domain.notification.DeviceTokenService
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.notification.InAppPushCenter
import com.kikepb.core.domain.notification.PushMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/** FCM entry point (spec 009). Background pushes are shown by the system; this handles the foreground ones. */
class SquadfyMessagingService : FirebaseMessagingService() {

    private val deviceTokenService by inject<DeviceTokenService>()
    private val sessionStorage by inject<SessionStorage>()
    private val applicationScope by inject<CoroutineScope>()
    private val inAppPushCenter by inject<InAppPushCenter>()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // AC-009-01: a renewed token is registered again while logged in
        applicationScope.launch {
            if (sessionStorage.observeAuthInfo().first() != null) deviceTokenService.registerToken(token = token, platform = "ANDROID")
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val push = PushMessage(
            data = message.data,
            title = message.notification?.title ?: message.data["title"],
            body = message.notification?.body ?: message.data["body"]
        )
        // AC-009-04: the visible club screen refreshes and shows a snackbar instead
        if (inAppPushCenter.onForegroundMessage(push)) return
        PushNotifier.show(this, push)
    }
}
