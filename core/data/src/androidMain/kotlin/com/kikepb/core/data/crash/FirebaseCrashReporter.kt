package com.kikepb.core.data.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kikepb.core.data.BuildKonfig
import com.kikepb.core.domain.crash.CrashReporter

/**
 * Crashlytics starts disabled (`firebase_crashlytics_collection_enabled = false` in the manifest) and is only
 * switched on in release builds once the user agreed (AC-011-13): debug crashes never reach the dashboard.
 */
class FirebaseCrashReporter : CrashReporter {
    override val isAvailable: Boolean = true

    override fun setCollectionEnabled(enabled: Boolean) {
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enabled && BuildKonfig.IS_RELEASE
    }
}
