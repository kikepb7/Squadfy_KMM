package com.kikepb.club.presentation.di

import com.kikepb.club.presentation.create.CreateClubViewModel
import com.kikepb.club.presentation.detail.ClubDetailViewModel
import com.kikepb.club.presentation.join.JoinClubViewModel
import com.kikepb.club.presentation.match.MatchViewModel
import com.kikepb.club.presentation.memberdetail.MemberDetailViewModel
import com.kikepb.club.presentation.settings.ScheduleSettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val clubPresentationModule = module {
    viewModelOf(::ClubDetailViewModel)
    viewModelOf(::JoinClubViewModel)
    viewModelOf(::CreateClubViewModel)
    viewModelOf(::MemberDetailViewModel)
    viewModelOf(::MatchViewModel)
    viewModelOf(::ScheduleSettingsViewModel)
}
