package com.kikepb.globalPosition.domain.di

import com.kikepb.globalPosition.domain.usecase.GetLatestNewsUseCase
import com.kikepb.globalPosition.domain.usecase.GetRecentMatchesUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val globalPositionDomainModule = module {
    factoryOf(::GetRecentMatchesUseCase)
    factoryOf(::GetLatestNewsUseCase)
}