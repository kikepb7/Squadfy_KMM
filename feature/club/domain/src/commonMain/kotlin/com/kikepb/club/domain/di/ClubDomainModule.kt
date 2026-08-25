package com.kikepb.club.domain.di

import com.kikepb.club.domain.usecase.AddGuestUseCase
import com.kikepb.club.domain.usecase.AddScheduleExceptionUseCase
import com.kikepb.club.domain.usecase.CanEditMemberProfileUseCase
import com.kikepb.club.domain.usecase.CancelSignupUseCase
import com.kikepb.club.domain.usecase.CreateClubUseCase
import com.kikepb.club.domain.usecase.CreateMatchUseCase
import com.kikepb.club.domain.usecase.FetchClubByIdUseCase
import com.kikepb.club.domain.usecase.FetchClubMembersUseCase
import com.kikepb.club.domain.usecase.GenerateTeamsUseCase
import com.kikepb.club.domain.usecase.GetClubByIdUseCase
import com.kikepb.club.domain.usecase.GetClubMatchesUseCase
import com.kikepb.club.domain.usecase.GetClubMemberByIdUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.JoinClubUseCase
import com.kikepb.club.domain.usecase.ListScheduleExceptionsUseCase
import com.kikepb.club.domain.usecase.ListSignupsUseCase
import com.kikepb.club.domain.usecase.RecordMatchResultUseCase
import com.kikepb.club.domain.usecase.RemoveScheduleExceptionUseCase
import com.kikepb.club.domain.usecase.RemoveSignupUseCase
import com.kikepb.club.domain.usecase.SignUpForMatchUseCase
import com.kikepb.club.domain.usecase.SyncClubDetailUseCase
import com.kikepb.club.domain.usecase.UpdateClubScheduleUseCase
import com.kikepb.club.domain.usecase.UploadClubLogoUseCase
import com.kikepb.club.domain.usecase.UploadMemberPhotoUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val clubDomainModule = module {
    singleOf(::CanEditMemberProfileUseCase)
    singleOf(::GetClubByIdUseCase)
    singleOf(::GetClubMembersUseCase)
    singleOf(::GetClubMemberByIdUseCase)
    singleOf(::FetchClubByIdUseCase)
    singleOf(::FetchClubMembersUseCase)
    singleOf(::JoinClubUseCase)
    singleOf(::SyncClubDetailUseCase)
    singleOf(::CreateClubUseCase)
    singleOf(::UploadClubLogoUseCase)
    singleOf(::UploadMemberPhotoUseCase)

    singleOf(::GetClubMatchesUseCase)
    singleOf(::CreateMatchUseCase)
    singleOf(::ListSignupsUseCase)
    singleOf(::SignUpForMatchUseCase)
    singleOf(::CancelSignupUseCase)
    singleOf(::AddGuestUseCase)
    singleOf(::RemoveSignupUseCase)
    singleOf(::GenerateTeamsUseCase)
    singleOf(::RecordMatchResultUseCase)
    singleOf(::UpdateClubScheduleUseCase)
    singleOf(::ListScheduleExceptionsUseCase)
    singleOf(::AddScheduleExceptionUseCase)
    singleOf(::RemoveScheduleExceptionUseCase)
}
