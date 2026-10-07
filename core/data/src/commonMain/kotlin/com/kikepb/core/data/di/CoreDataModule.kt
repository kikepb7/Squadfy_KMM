package com.kikepb.core.data.di

import com.kikepb.core.data.auth.KtorAuthRepositoryImpl
import com.kikepb.core.data.BuildKonfig
import com.kikepb.core.data.auth.storage.DataStoreSessionStorage
import com.kikepb.core.data.featureflag.DataStoreFeatureFlagOverrideStore
import com.kikepb.core.data.featureflag.DefaultFeatureFlags
import com.kikepb.core.data.featureflag.FeatureFlagOverrideStore
import com.kikepb.core.data.featureflag.NoOpRemoteFeatureFlagSource
import com.kikepb.core.data.logger.KermitLogger
import com.kikepb.core.data.networking.HttpClientFactory
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.featureflag.AppEnvironment
import com.kikepb.core.domain.featureflag.FeatureFlagOverrides
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.core.domain.featureflag.RemoteFeatureFlagSource
import com.kikepb.core.domain.logger.SquadfyLogger
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

expect val platformCoreDataModule: Module

val coreDataModule = module {
    includes(platformCoreDataModule)
    single<SquadfyLogger> { KermitLogger }
    single {
        HttpClientFactory(get(), get()).create(get())
    }
    singleOf(::KtorAuthRepositoryImpl) bind AuthRepository::class
    singleOf(::DataStoreSessionStorage) bind SessionStorage::class

    // Feature flags (spec 013): environment comes from SQUADFY_ENV at build time
    single { AppEnvironment.fromKey(BuildKonfig.ENVIRONMENT) }
    singleOf(::DataStoreFeatureFlagOverrideStore) bind FeatureFlagOverrideStore::class
    singleOf(::NoOpRemoteFeatureFlagSource) bind RemoteFeatureFlagSource::class
    single {
        DefaultFeatureFlags(environment = get(), overrideStore = get(), remoteSource = get(), scope = get())
    } binds arrayOf(FeatureFlags::class, FeatureFlagOverrides::class)
}