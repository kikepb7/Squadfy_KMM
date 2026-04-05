package com.kikepb.global_position.domain.di

import com.kikepb.globalPosition.domain.usecase.GetLatestNewsUseCase
import com.kikepb.globalPosition.domain.usecase.GetRecentMatchesUseCase
import com.kikepb.globalPosition.domain.usecase.GetUserClubsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val globalPositionDomainModule = module {
    factoryOf(::GetUserClubsUseCase)
    factoryOf(::GetRecentMatchesUseCase)
    factoryOf(::GetLatestNewsUseCase)
}