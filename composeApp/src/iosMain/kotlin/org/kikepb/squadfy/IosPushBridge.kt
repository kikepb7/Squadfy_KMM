package org.kikepb.squadfy

import com.kikepb.core.domain.notification.InAppPushCenter
import com.kikepb.core.domain.notification.PushMessage
import com.kikepb.core.domain.notification.PushRouter
import com.kikepb.core.domain.update.AppInfo
import org.kikepb.squadfy.navigation.ExternalUriHandler
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Called from `AppDelegate` (UNUserNotificationCenterDelegate): same routing as Android (AC-009-03/04/06). */
object IosPushBridge : KoinComponent {

    private val inAppPushCenter: InAppPushCenter by inject()
    private val appInfo: AppInfo by inject()

    fun onNotificationTapped(userInfo: Map<Any?, *>) {
        val data = userInfo.toData()
        PushRouter.deepLink(data)?.let { ExternalUriHandler.onNewUri(uri = it) }
        // Spec 021: a new version or an app announcement opens the App Store or the web page
        PushRouter.externalUrl(data)?.let { url -> NSURL.URLWithString(url)?.let { openUrl(it) } }
    }

    private fun openUrl(url: NSURL) = UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any?>(), completionHandler = null)

    /** False when a visible club screen already handled the push, so no banner is shown. */
    fun shouldPresentInForeground(userInfo: Map<Any?, *>, title: String?, body: String?): Boolean {
        val data = userInfo.toData()
        // Spec 021: this install already has the announced version
        if (PushRouter.isStaleUpdate(data, appInfo.versionName)) return false
        return !inAppPushCenter.onForegroundMessage(PushMessage(data = data, title = title, body = body))
    }

    private fun Map<Any?, *>.toData(): Map<String, String> =
        entries.mapNotNull { (key, value) -> (key as? String)?.let { k -> (value as? String)?.let { k to it } } }.toMap()
}
