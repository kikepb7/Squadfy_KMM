package com.kikepb.club.data.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kikepb.club.data.datasource.local.OfflineFirstClubRepositoryImpl
import com.kikepb.club.database.DatabaseFactory
import com.kikepb.club.domain.repository.ClubRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformClubDataModule: Module

val clubDataModule = module {
    includes(platformClubDataModule)

    single {
        get<DatabaseFactory>()
            .create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    singleOf(::OfflineFirstClubRepositoryImpl) bind ClubRepository::class
}
