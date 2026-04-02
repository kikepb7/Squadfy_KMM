package com.kikepb.club.data.di

import com.kikepb.club.data.datasource.remote.KtorClubRepositoryImpl
import com.kikepb.club.domain.repository.ClubRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val clubDataModule = module {
    singleOf(::KtorClubRepositoryImpl) bind ClubRepository::class
}
