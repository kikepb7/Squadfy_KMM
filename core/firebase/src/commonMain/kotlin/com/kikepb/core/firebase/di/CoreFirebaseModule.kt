package com.kikepb.core.firebase.di

import com.kikepb.core.domain.analytics.AnalyticsTracker
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.firebase.auth.FirebaseAuthRepository
import com.kikepb.core.firebase.auth.FirebaseSessionStorage
import com.kikepb.core.domain.crash.CrashReporter
import com.kikepb.core.domain.featureflag.RemoteFeatureFlagSource
import com.kikepb.core.firebase.BuildKonfig
import com.kikepb.core.firebase.analytics.FirebaseAnalyticsTracker
import com.kikepb.core.firebase.config.RemoteConfigFeatureFlagSource
import com.kikepb.core.firebase.crashlytics.FirebaseCrashReporter
import com.kikepb.core.firebase.functions.FunctionsClient
import com.kikepb.core.firebase.init.FirebaseInitializer
import com.kikepb.core.firebase.storage.FirebaseImageStorage
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.crashlytics.crashlytics
import dev.gitlive.firebase.database.database
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.functions.functions
import dev.gitlive.firebase.remoteconfig.remoteConfig
import dev.gitlive.firebase.storage.storage
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Firebase SDK entry points are singletons resolved lazily, so nothing touches Firebase until the first injection;
 * repositories receive them through their constructor and stay testable (spec 019, as in TensoScan).
 */
val coreFirebaseModule = module {
    single { Firebase.auth }
    single { Firebase.firestore }
    single { Firebase.database }
    single { Firebase.functions(FunctionsClient.REGION) }
    single { Firebase.storage }
    single { Firebase.remoteConfig }
    single { Firebase.analytics }
    single { Firebase.crashlytics }

    single { FunctionsClient(functions = get(), logger = get()) }

    // Account and session (spec 019 AC-019-04…06)
    single<AuthRepository> { FirebaseAuthRepository(auth = get(), firestore = inject(), functions = get()) }
    single<SessionStorage> { FirebaseSessionStorage(auth = get(), firestore = inject()) }
    single { FirebaseImageStorage(inject()) }

    single<CrashReporter> { FirebaseCrashReporter(crashlytics = inject(), isRelease = BuildKonfig.IS_RELEASE) }
    single<AnalyticsTracker> { FirebaseAnalyticsTracker(inject()) }
    single {
        // Development sees console changes within a minute; release keeps the SDK's throttling far away
        RemoteConfigFeatureFlagSource(
            remoteConfig = inject(),
            minimumFetchInterval = if (BuildKonfig.IS_RELEASE) 12.hours else 1.minutes,
            logger = get()
        )
    } bind RemoteFeatureFlagSource::class

    single {
        FirebaseInitializer(
            appScope = get(),
            auth = inject(),
            firestore = inject(),
            database = inject(),
            functions = inject(),
            storage = inject(),
            crashReporter = inject(),
            remoteFlags = inject(),
            versionName = BuildKonfig.VERSION_NAME,
            logger = get()
        )
    }
}
