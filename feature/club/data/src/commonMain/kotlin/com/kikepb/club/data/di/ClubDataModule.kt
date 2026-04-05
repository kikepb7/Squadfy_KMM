package com.kikepb.club.data.di

import com.kikepb.club.data.datasource.remote.KtorClubRepositoryImpl
import com.kikepb.club.domain.repository.ClubRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformClubDataModule: Module
val clubDataModule = module {
    includes(platformClubDataModule)
    singleOf(::KtorClubRepositoryImpl) bind ClubRepository::class
}
