package com.kikepb.club.domain.di

import com.kikepb.club.domain.usecase.CreateClubUseCase
import com.kikepb.club.domain.usecase.FetchClubByIdUseCase
import com.kikepb.club.domain.usecase.FetchClubMembersUseCase
import com.kikepb.club.domain.usecase.GetClubByIdUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.JoinClubUseCase
import com.kikepb.club.domain.usecase.UploadClubLogoUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val clubDomainModule = module {
    singleOf(::GetClubByIdUseCase)
    singleOf(::GetClubMembersUseCase)
    singleOf(::FetchClubByIdUseCase)
    singleOf(::FetchClubMembersUseCase)
    singleOf(::JoinClubUseCase)
    singleOf(::CreateClubUseCase)
    singleOf(::UploadClubLogoUseCase)
}
