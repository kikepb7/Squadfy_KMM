package com.kikepb.club.data.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kikepb.club.data.datasource.local.OfflineFirstClubRepositoryImpl
import com.kikepb.club.data.datasource.remote.KtorClubRepositoryImpl
import com.kikepb.club.database.DatabaseFactory
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.club.domain.repository.ClubService
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
            // The club/member schema is still evolving pre-launch; destructive migration is
            // acceptable until the app has real installs to preserve local cache for.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    singleOf(::KtorClubRepositoryImpl) bind ClubService::class
    singleOf(::OfflineFirstClubRepositoryImpl) bind ClubRepository::class
}
