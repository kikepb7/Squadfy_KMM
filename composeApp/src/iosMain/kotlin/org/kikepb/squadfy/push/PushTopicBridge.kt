package org.kikepb.squadfy.push

import com.kikepb.core.domain.notification.PushTopicSubscriber
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.dsl.module
import kotlin.coroutines.resume

/**
 * Implemented in Swift (`AppDelegate.swift`) with `Messaging.messaging().subscribe(toTopic:)`, which needs the
 * Firebase Messaging SDK. [onDone] is called once with whether it worked.
 */
interface PushTopicBridge {
    fun subscribe(topic: String, onDone: (Boolean) -> Unit)
    fun unsubscribe(topic: String, onDone: (Boolean) -> Unit)
}

/** Where Swift registers its bridge at launch. Exposed to Swift as `IosPushTopics.shared`. */
object IosPushTopics {
    var bridge: PushTopicBridge? = null
}

/** Spec 021 AC-021-07 on iOS. Without a bridge (or before the APNs token) it does nothing; the next token retries. */
class IosPushTopicSubscriber : PushTopicSubscriber {

    override suspend fun subscribe(topic: String): Boolean = call { bridge, onDone -> bridge.subscribe(topic, onDone) }

    override suspend fun unsubscribe(topic: String): Boolean = call { bridge, onDone -> bridge.unsubscribe(topic, onDone) }

    private suspend fun call(action: (PushTopicBridge, (Boolean) -> Unit) -> Unit): Boolean {
        val bridge = IosPushTopics.bridge ?: return false
        return suspendCancellableCoroutine { continuation ->
            action(bridge) { success -> if (continuation.isActive) continuation.resume(success) }
        }
    }
}

internal actual val platformPushTopicsModule = module {
    single<PushTopicSubscriber> { IosPushTopicSubscriber() }
}
