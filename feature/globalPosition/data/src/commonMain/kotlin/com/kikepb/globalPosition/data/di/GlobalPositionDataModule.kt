package com.kikepb.globalPosition.data.di

import com.kikepb.globalPosition.data.datasource.local.OfflineFirstGlobalPositionRepositoryImpl
import com.kikepb.globalPosition.data.datasource.remote.KtorGlobalPositionRepositoryImpl
import com.kikepb.globalPosition.domain.repository.GlobalPositionRepository
import com.kikepb.globalPosition.domain.repository.GlobalPositionService
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformGlobalPositionDataModule: Module

val globalPositionDataModule = module {
    includes(platformGlobalPositionDataModule)
    singleOf(::KtorGlobalPositionRepositoryImpl) bind GlobalPositionService::class
    singleOf(::OfflineFirstGlobalPositionRepositoryImpl) bind GlobalPositionRepository::class
}