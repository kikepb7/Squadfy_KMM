package org.kikepb.squadfy.push

import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.kikepb.core.domain.logger.SquadfyLogger
import com.kikepb.core.domain.notification.PushTopicSubscriber
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.dsl.module
import kotlin.coroutines.resume

/** Firebase Messaging topics on Android (spec 021 AC-021-07). A failure is only logged: the next launch retries. */
class AndroidPushTopicSubscriber(private val logger: SquadfyLogger) : PushTopicSubscriber {

    override suspend fun subscribe(topic: String): Boolean = FirebaseMessaging.getInstance().subscribeToTopic(topic).awaitSuccess(topic)

    override suspend fun unsubscribe(topic: String): Boolean = FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).awaitSuccess(topic)

    private suspend fun Task<Void>.awaitSuccess(topic: String): Boolean = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (!task.isSuccessful) logger.warn("FCM topic $topic failed: ${task.exception?.message}")
            continuation.resume(task.isSuccessful)
        }
    }
}

internal actual val platformPushTopicsModule = module {
    single<PushTopicSubscriber> { AndroidPushTopicSubscriber(logger = get()) }
}
