package com.kikepb.chat.data.notification

import com.kikepb.chat.domain.notification.PushNotificationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIApplication
import platform.UIKit.registerForRemoteNotifications

actual class FirebasePushNotificationRepositoryImpl :
    PushNotificationService {
    actual override fun observeDeviceToken(): Flow<String?> =
        IosDeviceTokenHolder.token
            .onStart {
                if (IosDeviceTokenHolder.token.value == null) {
                    val userDefaults = NSUserDefaults.standardUserDefaults
                    val fcmToken = userDefaults.stringForKey("FCM_TOKEN")

                    if (fcmToken != null) {
                        IosDeviceTokenHolder.updateToken(token = fcmToken)
                    } else {
                        UIApplication.sharedApplication.registerForRemoteNotifications()
                    }
                }
            }
}