package com.kikepb.core.domain.notification

import com.kikepb.core.domain.featureflag.AppEnvironment
import com.kikepb.core.domain.featureflag.AppPlatform

/**
 * FCM topics of the app-wide notices (spec 021 APP-RN-P7): `app_<platform>_<lang>`, so a new version reaches only the
 * platform it was released for, in the device's language. Dev builds (PRE) share the Firebase project, so theirs carry
 * a `pre_` prefix and never get a notice meant for the store app. The owner sends them with
 * `firebase/functions/scripts/send-app-notice.mjs`.
 */
object PushTopics {

    val LANGUAGES = listOf("es", "en")

    /** Same rule as the Functions (`langOf`): English for devices in English, Spanish for everyone else (APP-RN-P2). */
    fun languageOf(localeTag: String): String = if (localeTag.lowercase().startsWith("en")) "en" else "es"

    fun app(environment: AppEnvironment, platform: AppPlatform, language: String): String {
        val prefix = if (environment == AppEnvironment.PRE) "pre_" else ""
        return "${prefix}app_${platform.name.lowercase()}_$language"
    }
}

/** Subscribes this device to FCM topics. Implemented natively: Firebase Messaging on Android, Swift on iOS. */
interface PushTopicSubscriber {
    suspend fun subscribe(topic: String): Boolean
    suspend fun unsubscribe(topic: String): Boolean
}

/**
 * AC-021-07: at every launch the device joins the topic of its platform and language and leaves the one of the other
 * language, so a language change in the system settings moves it. Both calls are idempotent in FCM.
 */
class SyncPushTopicsUseCase(
    private val subscriber: PushTopicSubscriber,
    private val environment: AppEnvironment,
    private val platform: AppPlatform,
    private val localeTag: () -> String
) {
    suspend operator fun invoke() {
        val language = PushTopics.languageOf(localeTag())
        subscriber.subscribe(PushTopics.app(environment, platform, language))
        PushTopics.LANGUAGES.filter { it != language }.forEach { subscriber.unsubscribe(PushTopics.app(environment, platform, it)) }
    }
}
