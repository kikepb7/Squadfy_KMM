package com.kikepb.core.firebase.init

import co.touchlab.kermit.Logger
import com.kikepb.core.domain.crash.CrashReporter
import com.kikepb.core.domain.logger.SquadfyLogger
import com.kikepb.core.firebase.BuildKonfig
import com.kikepb.core.firebase.config.RemoteConfigFeatureFlagSource
import com.kikepb.core.firebase.crashlytics.CrashReportingLogWriter
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.app
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.functions.FirebaseFunctions
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * One-time Firebase bootstrap (spec 019), run right after Koin starts and before any screen touches Firebase:
 * emulator wiring, crash-reporting logger, keys and user id, and the Remote Config fetch for the feature flags.
 *
 * Firebase itself is initialized by the platform: the google-services plugin on Android and `FirebaseApp.configure()`
 * in `AppDelegate.swift` on iOS.
 */
class FirebaseInitializer(
    private val appScope: CoroutineScope,
    private val auth: Lazy<FirebaseAuth>,
    private val firestore: Lazy<FirebaseFirestore>,
    private val database: Lazy<FirebaseDatabase>,
    private val functions: Lazy<FirebaseFunctions>,
    private val storage: Lazy<FirebaseStorage>,
    // Lazy, and first resolved off the main thread: none of them is needed for the first frame
    private val crashReporter: Lazy<CrashReporter>,
    private val remoteFlags: Lazy<RemoteConfigFeatureFlagSource>,
    private val versionName: String,
    private val logger: SquadfyLogger
) {

    fun initialize() {
        if (!isFirebaseConfigured()) {
            logger.error(
                "Firebase is not configured: add composeApp/google-services.json and " +
                    "iosApp/Firebase/<Configuration>/GoogleService-Info.plist (see docs/FIREBASE.md)"
            )
            return
        }

        // Before any other call: the SDKs refuse to switch to the emulator once they have talked to production
        if (BuildKonfig.FIREBASE_USE_EMULATOR) {
            useEmulators(host = BuildKonfig.FIREBASE_EMULATOR_HOST.ifBlank { defaultEmulatorHost })
        }

        appScope.launch {
            val crashes = crashReporter.value
            installCrashHandler()
            Logger.addLogWriter(CrashReportingLogWriter(crashes))
            crashes.setCustomKey(KEY_APP_VERSION, versionName)
            crashes.setCustomKey(KEY_ENVIRONMENT, BuildKonfig.ENVIRONMENT)
        }

        // Crash reports carry the (pseudonymous) Firebase uid so a support request can be matched; Analytics does not
        // need to know who the user is, so it gets no user id
        appScope.launch {
            auth.value.authStateChanged
                .map { it?.uid }
                .distinctUntilChanged()
                .collect { uid -> crashReporter.value.setUserId(uid) }
        }

        appScope.launch { remoteFlags.value.refresh() }
    }

    /** Called when the app returns to the foreground, so a flag switched in the console arrives without a restart. */
    fun onForeground() {
        if (!isFirebaseConfigured()) return
        appScope.launch { remoteFlags.value.refresh() }
    }

    private fun useEmulators(host: String) {
        logger.info("Using the Firebase Local Emulator Suite at $host")
        auth.value.useEmulator(host, EmulatorPorts.AUTH)
        firestore.value.useEmulator(host, EmulatorPorts.FIRESTORE)
        database.value.useEmulator(host, EmulatorPorts.DATABASE)
        functions.value.useEmulator(host, EmulatorPorts.FUNCTIONS)
        storage.value.useEmulator(host, EmulatorPorts.STORAGE)
    }

    private fun isFirebaseConfigured(): Boolean = try {
        Firebase.app
        true
    } catch (_: Throwable) {
        false
    }

    private companion object {
        const val KEY_APP_VERSION = "app_version"
        const val KEY_ENVIRONMENT = "environment"
    }
}

/**
 * Spec 019 AC-019-20: App Check attests the app to the real project (debug provider in debug builds, Play Integrity
 * and App Attest in release). The emulators do not verify it, so it is not installed while using them. The provider
 * itself is installed natively, before Firebase starts: `SquadfyApplication` on Android, `AppDelegate.swift` on iOS.
 */
val isAppCheckNeeded: Boolean get() = !BuildKonfig.FIREBASE_USE_EMULATOR

/** Must match `firebase/firebase.json`. Not the usual defaults, so they never clash with TensoScan or the own backend. */
object EmulatorPorts {
    const val AUTH = 9098
    const val FIRESTORE = 8086
    const val DATABASE = 9001
    const val FUNCTIONS = 5003
    const val STORAGE = 9198
}

/** 10.0.2.2 from the Android emulator, localhost from the iOS simulator. */
internal expect val defaultEmulatorHost: String

/** iOS: CrashKiOS turns uncaught Kotlin exceptions into Crashlytics reports with a real stack. Android: nothing. */
internal expect fun installCrashHandler()
