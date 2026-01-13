package com.kikepb.chat.data.notification

import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.kikepb.chat.domain.notification.PushNotificationService
import com.kikepb.core.domain.logger.SquadfyLogger
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import kotlin.coroutines.coroutineContext

actual class FirebasePushNotificationRepositoryImpl(
    private val logger: SquadfyLogger
) :
    PushNotificationService {
    actual override fun observeDeviceToken(): Flow<String?> = flow {
        try {
            val fcmToken = Firebase.messaging.token.await()
            logger.info(message = "Initial FCM token received: $fcmToken")
            emit(value = fcmToken)
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            logger.error(message = "Failed to get FCM token", e)
            emit(value = null)
        }
    }
}