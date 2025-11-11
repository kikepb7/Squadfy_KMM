package com.kikepb.core.data.di

import com.kikepb.core.data.auth.KtorAuthRepositoryImpl
import com.kikepb.core.data.logger.KermitLogger
import com.kikepb.core.data.networking.HttpClientFactory
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.logger.SquadfyLogger
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformCoreDataModule: Module

val coreDataModule = module {
    includes(platformCoreDataModule)
    single<SquadfyLogger> { KermitLogger }
    single {
        HttpClientFactory(get()).create(get())
    }
    singleOf(::KtorAuthRepositoryImpl) bind AuthRepository::class
}