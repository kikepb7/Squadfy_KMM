package com.kikepb.club.data.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kikepb.club.data.datasource.local.OfflineFirstClubRepositoryImpl
import com.kikepb.club.data.datasource.remote.KtorAnnouncementRepository
import com.kikepb.club.data.datasource.remote.KtorMatchRepository
import com.kikepb.club.data.datasource.remote.KtorScheduleRepository
import com.kikepb.club.data.datasource.remote.KtorStandingsRepository
import com.kikepb.club.database.DatabaseFactory
import com.kikepb.club.database.SquadfyClubDatabase
import com.kikepb.club.database.migration.ClubDatabaseMigrations
import com.kikepb.club.domain.repository.AnnouncementRepository
import com.kikepb.club.domain.repository.ClubRepository
import com.kikepb.club.domain.repository.MatchRepository
import com.kikepb.club.domain.repository.ScheduleRepository
import com.kikepb.club.domain.repository.StandingsRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformClubDataModule: Module

val clubDataModule = module {
    includes(platformClubDataModule)
    single<SquadfyClubDatabase> {
        get<DatabaseFactory>()
            .create()
            .setDriver(BundledSQLiteDriver())
            // Explicit migrations only: the schema is exported in feature/club/database/schemas (constitution III.4)
            .addMigrations(*ClubDatabaseMigrations.ALL)
            .build()
    }
    singleOf(::OfflineFirstClubRepositoryImpl) bind ClubRepository::class
    singleOf(::KtorScheduleRepository) bind ScheduleRepository::class
    singleOf(::KtorAnnouncementRepository) bind AnnouncementRepository::class
    singleOf(::KtorMatchRepository) bind MatchRepository::class
    singleOf(::KtorStandingsRepository) bind StandingsRepository::class
}
