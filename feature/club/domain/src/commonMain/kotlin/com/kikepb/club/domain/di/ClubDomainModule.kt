package com.kikepb.club.domain.di

import com.kikepb.club.domain.usecase.AddGuestUseCase
import com.kikepb.club.domain.usecase.AddScheduleExceptionUseCase
import com.kikepb.club.domain.usecase.DeleteScheduleExceptionUseCase
import com.kikepb.club.domain.usecase.GetScheduleExceptionsUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.usecase.SaveScheduleUseCase
import com.kikepb.club.domain.usecase.BanMemberUseCase
import com.kikepb.club.domain.usecase.CancelSignupUseCase
import com.kikepb.club.domain.usecase.ChangeMemberRoleUseCase
import com.kikepb.club.domain.usecase.CreateClubUseCase
import com.kikepb.club.domain.usecase.CreateMatchUseCase
import com.kikepb.club.domain.usecase.EditClubUseCase
import com.kikepb.club.domain.usecase.FetchMyClubsUseCase
import com.kikepb.club.domain.usecase.GenerateTeamsUseCase
import com.kikepb.club.domain.usecase.GetClubBansUseCase
import com.kikepb.club.domain.usecase.GetClubByIdUseCase
import com.kikepb.club.domain.usecase.GetClubMatchesUseCase
import com.kikepb.club.domain.usecase.GetClubMemberByIdUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.JoinClubUseCase
import com.kikepb.club.domain.usecase.LeaveClubUseCase
import com.kikepb.club.domain.usecase.ListSignupsUseCase
import com.kikepb.club.domain.usecase.ObserveMyClubsUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.domain.usecase.RecordMatchResultUseCase
import com.kikepb.club.domain.usecase.RegenerateInvitationCodeUseCase
import com.kikepb.club.domain.usecase.RemoveMemberUseCase
import com.kikepb.club.domain.usecase.RemoveSignupUseCase
import com.kikepb.club.domain.usecase.SignUpForMatchUseCase
import com.kikepb.club.domain.usecase.SyncClubDetailUseCase
import com.kikepb.club.domain.usecase.TransferOwnershipUseCase
import com.kikepb.club.domain.usecase.UnbanMemberUseCase
import com.kikepb.club.domain.usecase.UpdateMyMembershipUseCase
import com.kikepb.club.domain.usecase.UploadClubLogoUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val clubDomainModule = module {
    // Clubs and membership (spec 003)
    singleOf(::ObserveMyClubsUseCase)
    singleOf(::FetchMyClubsUseCase)
    singleOf(::GetClubByIdUseCase)
    singleOf(::GetClubMembersUseCase)
    singleOf(::GetClubMemberByIdUseCase)
    singleOf(::ObserveMyMembershipUseCase)
    singleOf(::SyncClubDetailUseCase)
    singleOf(::CreateClubUseCase)
    singleOf(::JoinClubUseCase)
    singleOf(::UploadClubLogoUseCase)
    singleOf(::EditClubUseCase)
    singleOf(::RegenerateInvitationCodeUseCase)
    singleOf(::UpdateMyMembershipUseCase)
    singleOf(::LeaveClubUseCase)
    singleOf(::RemoveMemberUseCase)
    singleOf(::ChangeMemberRoleUseCase)
    singleOf(::TransferOwnershipUseCase)
    singleOf(::GetClubBansUseCase)
    singleOf(::BanMemberUseCase)
    singleOf(::UnbanMemberUseCase)

    // Weekly schedule (spec 004)
    singleOf(::GetScheduleUseCase)
    singleOf(::SaveScheduleUseCase)
    singleOf(::GetScheduleExceptionsUseCase)
    singleOf(::AddScheduleExceptionUseCase)
    singleOf(::DeleteScheduleExceptionUseCase)

    // Legacy match flow, replaced in specs 005-007
    singleOf(::GetClubMatchesUseCase)
    singleOf(::CreateMatchUseCase)
    singleOf(::ListSignupsUseCase)
    singleOf(::SignUpForMatchUseCase)
    singleOf(::CancelSignupUseCase)
    singleOf(::AddGuestUseCase)
    singleOf(::RemoveSignupUseCase)
    singleOf(::GenerateTeamsUseCase)
    singleOf(::RecordMatchResultUseCase)
}
