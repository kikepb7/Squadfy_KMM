package com.kikepb.core.firebase.crashlytics

import com.kikepb.core.domain.crash.CrashReporter
import dev.gitlive.firebase.crashlytics.FirebaseCrashlytics

/**
 * Crashlytics on Android and iOS (spec 019 AC-019-18). It starts disabled (manifest / Info.plist) and is only switched
 * on in release builds once the user agreed (AC-011-13): debug crashes never reach the dashboard.
 */
class FirebaseCrashReporter(
    private val crashlytics: Lazy<FirebaseCrashlytics>,
    private val isRelease: Boolean
) : CrashReporter {

    override val isAvailable: Boolean = true

    override fun setCollectionEnabled(enabled: Boolean) =
        crashlytics.value.setCrashlyticsCollectionEnabled(enabled && isRelease)

    override fun log(message: String) = crashlytics.value.log(message)

    override fun recordException(throwable: Throwable, customKeys: Map<String, String>) {
        if (customKeys.isEmpty()) crashlytics.value.recordException(throwable)
        else crashlytics.value.recordException(throwable, customKeys)
    }

    // Crashlytics has no "clear" call; an empty id detaches future reports from the previous user
    override fun setUserId(userId: String?) = crashlytics.value.setUserId(userId.orEmpty())

    override fun setCustomKey(key: String, value: String) = crashlytics.value.setCustomKey(key, value)
}
