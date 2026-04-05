package com.kikepb.globalPosition.data.di

import com.kikepb.globalPosition.data.datasource.remote.KtorGlobalPositionRepositoryImpl
import com.kikepb.globalPosition.domain.repository.GlobalPositionRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

expect val platformGlobalPositionDataModule: Module

val globalPositionDataModule = module {
    includes(platformGlobalPositionDataModule)
    singleOf(::KtorGlobalPositionRepositoryImpl) { bind<GlobalPositionRepository>() }
}