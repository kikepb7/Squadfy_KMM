package com.kikepb.globalPosition.domain.di

import com.kikepb.globalPosition.domain.usecase.FetchUserClubsUseCase
import com.kikepb.globalPosition.domain.usecase.GetCurrentUserUseCase
import com.kikepb.globalPosition.domain.usecase.GetLatestNewsUseCase
import com.kikepb.globalPosition.domain.usecase.GetNextMatchUseCase
import com.kikepb.globalPosition.domain.usecase.GetRecentMatchesUseCase
import com.kikepb.globalPosition.domain.usecase.GetUserClubsUseCase
import com.kikepb.globalPosition.domain.usecase.ToggleMatchParticipationUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val globalPositionDomainModule = module {
    factoryOf(::GetUserClubsUseCase)
    factoryOf(::FetchUserClubsUseCase)
    factoryOf(::GetNextMatchUseCase)
    factoryOf(::ToggleMatchParticipationUseCase)
    factoryOf(::GetRecentMatchesUseCase)
    factoryOf(::GetLatestNewsUseCase)
    factoryOf(::GetCurrentUserUseCase)
}