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
        // Only the Firebase call is guarded: wrapping emit() would also catch the abort of a collector that
        // stops early (e.g. firstOrNull() when signing out) and emit again, which crashes (flow transparency).
        val fcmToken = try {
            Firebase.messaging.token.await().also {
                // AC-011-04: the device token is never logged
                logger.info(message = "Initial FCM token received")
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            logger.error(message = "Failed to get FCM token", e)
            null
        }
        emit(value = fcmToken)
    }
}