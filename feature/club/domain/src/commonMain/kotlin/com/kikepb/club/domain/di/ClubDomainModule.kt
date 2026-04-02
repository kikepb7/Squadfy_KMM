package com.kikepb.club.domain.di

import com.kikepb.club.domain.usecase.CreateClubUseCase
import com.kikepb.club.domain.usecase.GetClubByIdUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.GetUserClubsUseCase
import com.kikepb.club.domain.usecase.JoinClubUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val clubDomainModule = module {
    singleOf(::GetUserClubsUseCase)
    singleOf(::GetClubByIdUseCase)
    singleOf(::GetClubMembersUseCase)
    singleOf(::CreateClubUseCase)
    singleOf(::JoinClubUseCase)
}
