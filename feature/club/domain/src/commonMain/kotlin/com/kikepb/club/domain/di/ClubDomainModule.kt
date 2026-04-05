package com.kikepb.club.domain.di

import com.kikepb.club.domain.usecase.GetClubByIdUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val clubDomainModule = module {
    singleOf(::GetClubByIdUseCase)
    singleOf(::GetClubMembersUseCase)
}
