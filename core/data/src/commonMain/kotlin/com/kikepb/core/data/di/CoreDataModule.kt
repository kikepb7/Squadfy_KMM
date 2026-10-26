package com.kikepb.core.data.di

import com.kikepb.core.data.auth.KtorAuthRepositoryImpl
import com.kikepb.core.data.BuildKonfig
import com.kikepb.core.data.auth.storage.DataStoreSessionStorage
import com.kikepb.core.data.featureflag.DataStoreFeatureFlagOverrideStore
import com.kikepb.core.data.notification.DataStoreNotificationPromptStore
import com.kikepb.core.domain.notification.InAppPushCenter
import com.kikepb.core.domain.notification.NotificationPromptStore
import com.kikepb.core.data.featureflag.DefaultFeatureFlags
import com.kikepb.core.data.featureflag.FeatureFlagOverrideStore
import com.kikepb.core.data.featureflag.NoOpRemoteFeatureFlagSource
import com.kikepb.core.data.util.currentAppPlatform
import com.kikepb.core.data.logger.KermitLogger
import com.kikepb.core.data.networking.HttpClientFactory
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.config.LegalLinks
import com.kikepb.core.data.crash.DataStoreCrashReportingConsent
import com.kikepb.core.domain.crash.CrashReportingConsent
import com.kikepb.core.domain.featureflag.AppEnvironment
import com.kikepb.core.domain.featureflag.FeatureFlagOverrides
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.core.domain.featureflag.RemoteFeatureFlagSource
import com.kikepb.core.domain.logger.SquadfyLogger
import com.kikepb.core.domain.analytics.AnalyticsTracker
import com.kikepb.core.domain.analytics.NoOpAnalyticsTracker
import org.koin.core.module.Module
import kotlin.time.Clock
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

expect val platformCoreDataModule: Module
expect val platformCoreApiDataModule: Module
expect val platformCoreApiTelemetryModule: Module

/** Shared by both data layers (spec 019 AC-019-01): DataStore, logger, clock, feature flags, consent and push prompt. */
val coreDataModule = module {
    includes(platformCoreDataModule)
    single<SquadfyLogger> { KermitLogger }

    // Wall clock for time-based rules (announcement window countdown, spec 005)
    single<Clock> { Clock.System }

    // Feature flags (spec 013): environment comes from SQUADFY_ENV at build time
    single { AppEnvironment.fromKey(BuildKonfig.ENVIRONMENT) }
    single { LegalLinks(privacyPolicyUrl = BuildKonfig.PRIVACY_POLICY_URL, accountDeletionUrl = BuildKonfig.ACCOUNT_DELETION_URL) }
    singleOf(::DataStoreFeatureFlagOverrideStore) bind FeatureFlagOverrideStore::class
    // Push notifications (spec 009)
    single { InAppPushCenter() }
    singleOf(::DataStoreNotificationPromptStore) bind NotificationPromptStore::class
    // Crash reports with consent (spec 011 AC-011-13)
    singleOf(::DataStoreCrashReportingConsent) bind CrashReportingConsent::class
    single {
        DefaultFeatureFlags(environment = get(), platform = currentAppPlatform, overrideStore = get(), remoteSource = get(), scope = get())
    } binds arrayOf(FeatureFlags::class, FeatureFlagOverrides::class)
}

/** The Ktor client of the own backend. Spec 019 keeps it in the Firebase variant only for features not ported yet. */
val coreApiHttpModule = module {
    includes(platformCoreApiDataModule)
    single {
        HttpClientFactory(get(), get()).create(get())
    }
}

/** The own backend (frozen, ADR-0010): Ktor client with JWT refresh and the encrypted session store. */
val coreApiNetworkModule = module {
    includes(coreApiHttpModule)
    singleOf(::KtorAuthRepositoryImpl) bind AuthRepository::class
    singleOf(::DataStoreSessionStorage) bind SessionStorage::class
}

/** What Firebase provides in the other variant: crash reporter, no remote flags (D-10 is Remote Config) and no analytics. */
val coreApiTelemetryModule = module {
    includes(platformCoreApiTelemetryModule)
    singleOf(::NoOpRemoteFeatureFlagSource) bind RemoteFeatureFlagSource::class
    single<AnalyticsTracker> { NoOpAnalyticsTracker }
}

