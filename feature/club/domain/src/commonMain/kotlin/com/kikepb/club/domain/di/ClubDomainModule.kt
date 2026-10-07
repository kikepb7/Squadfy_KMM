package com.kikepb.club.domain.di

import com.kikepb.club.domain.usecase.AddGuestToAnnouncementUseCase
import com.kikepb.club.domain.usecase.AddScheduleExceptionUseCase
import com.kikepb.club.domain.usecase.EnrollUseCase
import com.kikepb.club.domain.usecase.GetAnnouncementHistoryUseCase
import com.kikepb.club.domain.usecase.GetCurrentAnnouncementUseCase
import com.kikepb.club.domain.usecase.RemoveGuestFromAnnouncementUseCase
import com.kikepb.club.domain.usecase.WithdrawUseCase
import com.kikepb.club.domain.usecase.DeleteScheduleExceptionUseCase
import com.kikepb.club.domain.usecase.GetScheduleExceptionsUseCase
import com.kikepb.club.domain.usecase.GetScheduleUseCase
import com.kikepb.club.domain.usecase.SaveScheduleUseCase
import com.kikepb.club.domain.usecase.BanMemberUseCase
import com.kikepb.club.domain.usecase.ChangeMemberRoleUseCase
import com.kikepb.club.domain.usecase.CreateClubUseCase
import com.kikepb.club.domain.usecase.EditClubUseCase
import com.kikepb.club.domain.usecase.FetchMyClubsUseCase
import com.kikepb.club.domain.usecase.GetClubBansUseCase
import com.kikepb.club.domain.usecase.GetClubByIdUseCase
import com.kikepb.club.domain.usecase.GetClubMemberByIdUseCase
import com.kikepb.club.domain.usecase.GetClubMembersUseCase
import com.kikepb.club.domain.usecase.JoinClubUseCase
import com.kikepb.club.domain.usecase.LeaveClubUseCase
import com.kikepb.club.domain.usecase.ObserveMyClubsUseCase
import com.kikepb.club.domain.usecase.ObserveMyMembershipUseCase
import com.kikepb.club.domain.usecase.RegenerateInvitationCodeUseCase
import com.kikepb.club.domain.usecase.RemoveMemberUseCase
import com.kikepb.club.domain.usecase.SyncClubDetailUseCase
import com.kikepb.club.domain.usecase.TransferOwnershipUseCase
import com.kikepb.club.domain.usecase.UnbanMemberUseCase
import com.kikepb.club.domain.usecase.UpdateMyMembershipUseCase
import com.kikepb.club.domain.usecase.UploadClubLogoUseCase
import com.kikepb.club.domain.usecase.GetMatchUseCase
import com.kikepb.club.domain.usecase.GetMatchAnnouncementUseCase
import com.kikepb.club.domain.usecase.GetClubMatchesUseCase
import com.kikepb.club.domain.usecase.GetTeamBalanceUseCase
import com.kikepb.club.domain.usecase.GenerateTeamsUseCase
import com.kikepb.club.domain.usecase.CreateExtraMatchUseCase
import com.kikepb.club.domain.usecase.CancelMatchUseCase
import com.kikepb.club.domain.usecase.CompleteMatchUseCase
import com.kikepb.club.domain.usecase.ReopenMatchUseCase
import com.kikepb.club.domain.usecase.AddMatchEventUseCase
import com.kikepb.club.domain.usecase.DeleteMatchEventUseCase
import com.kikepb.club.domain.usecase.SetPlayerMinutesUseCase
import com.kikepb.club.domain.usecase.SetManualScoreUseCase
import com.kikepb.club.domain.usecase.ClearManualScoreUseCase
import com.kikepb.club.domain.usecase.GetRatingLeaderboardUseCase
import com.kikepb.club.domain.usecase.GetMyRatingUseCase
import com.kikepb.club.domain.usecase.GetStatsLeaderboardUseCase
import com.kikepb.club.domain.usecase.GetMyStatsUseCase
import com.kikepb.club.domain.usecase.GetRecentRatingChangesUseCase
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

    // Announcement and enrollment (spec 005)
    singleOf(::GetCurrentAnnouncementUseCase)
    singleOf(::GetAnnouncementHistoryUseCase)
    singleOf(::EnrollUseCase)
    singleOf(::WithdrawUseCase)
    singleOf(::AddGuestToAnnouncementUseCase)
    singleOf(::RemoveGuestFromAnnouncementUseCase)

    // Matches, teams and result (specs 006/007)
    singleOf(::GetMatchUseCase)
    singleOf(::GetMatchAnnouncementUseCase)
    singleOf(::GetClubMatchesUseCase)
    singleOf(::GetTeamBalanceUseCase)
    singleOf(::GenerateTeamsUseCase)
    singleOf(::CreateExtraMatchUseCase)
    singleOf(::CancelMatchUseCase)
    singleOf(::CompleteMatchUseCase)
    singleOf(::ReopenMatchUseCase)
    singleOf(::AddMatchEventUseCase)
    singleOf(::DeleteMatchEventUseCase)
    singleOf(::SetPlayerMinutesUseCase)
    singleOf(::SetManualScoreUseCase)
    singleOf(::ClearManualScoreUseCase)

    // Ratings and stats (spec 008)
    singleOf(::GetRatingLeaderboardUseCase)
    singleOf(::GetMyRatingUseCase)
    singleOf(::GetStatsLeaderboardUseCase)
    singleOf(::GetMyStatsUseCase)
    singleOf(::GetRecentRatingChangesUseCase)
}
