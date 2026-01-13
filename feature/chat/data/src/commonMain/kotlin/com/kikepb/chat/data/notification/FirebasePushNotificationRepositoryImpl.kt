package com.kikepb.chat.data.notification

import com.kikepb.chat.domain.notification.PushNotificationService
import kotlinx.coroutines.flow.Flow

expect class FirebasePushNotificationRepositoryImpl: PushNotificationService {
    override fun observeDeviceToken(): Flow<String?>
}