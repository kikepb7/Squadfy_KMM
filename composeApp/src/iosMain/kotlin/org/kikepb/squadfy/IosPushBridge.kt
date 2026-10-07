package org.kikepb.squadfy

import com.kikepb.core.domain.notification.InAppPushCenter
import com.kikepb.core.domain.notification.PushMessage
import com.kikepb.core.domain.notification.PushRouter
import org.kikepb.squadfy.navigation.ExternalUriHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Called from `AppDelegate` (UNUserNotificationCenterDelegate): same routing as Android (AC-009-03/04/06). */
object IosPushBridge : KoinComponent {

    private val inAppPushCenter: InAppPushCenter by inject()

    fun onNotificationTapped(userInfo: Map<Any?, *>) {
        PushRouter.deepLink(userInfo.toData())?.let { ExternalUriHandler.onNewUri(uri = it) }
    }

    /** False when a visible club screen already handled the push, so no banner is shown. */
    fun shouldPresentInForeground(userInfo: Map<Any?, *>, title: String?, body: String?): Boolean =
        !inAppPushCenter.onForegroundMessage(PushMessage(data = userInfo.toData(), title = title, body = body))

    private fun Map<Any?, *>.toData(): Map<String, String> =
        entries.mapNotNull { (key, value) -> (key as? String)?.let { k -> (value as? String)?.let { k to it } } }.toMap()
}
