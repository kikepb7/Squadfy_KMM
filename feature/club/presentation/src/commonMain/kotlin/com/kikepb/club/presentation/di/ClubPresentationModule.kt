package com.kikepb.club.presentation.di

import com.kikepb.club.presentation.announcement.AnnouncementViewModel
import com.kikepb.club.presentation.bans.ClubBansViewModel
import com.kikepb.club.presentation.clubs.ClubsListViewModel
import com.kikepb.club.presentation.create.CreateClubViewModel
import com.kikepb.club.presentation.detail.ClubDetailViewModel
import com.kikepb.club.presentation.extramatch.ExtraMatchViewModel
import com.kikepb.club.presentation.join.JoinClubViewModel
import com.kikepb.club.presentation.match.MatchDetailViewModel
import com.kikepb.club.presentation.memberdetail.MemberDetailViewModel
import com.kikepb.club.presentation.schedule.ScheduleViewModel
import com.kikepb.club.presentation.settings.ClubSettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val clubPresentationModule = module {
    viewModelOf(::ClubsListViewModel)
    viewModelOf(::ClubDetailViewModel)
    viewModelOf(::JoinClubViewModel)
    viewModelOf(::CreateClubViewModel)
    viewModelOf(::MemberDetailViewModel)
    viewModelOf(::ClubSettingsViewModel)
    viewModelOf(::ClubBansViewModel)
    viewModelOf(::ScheduleViewModel)
    viewModelOf(::AnnouncementViewModel)
    viewModelOf(::MatchDetailViewModel)
    viewModelOf(::ExtraMatchViewModel)
}
