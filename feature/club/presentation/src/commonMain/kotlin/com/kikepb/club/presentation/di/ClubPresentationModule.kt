package com.kikepb.club.presentation.di

import com.kikepb.club.presentation.clubs.detail.ClubDetailViewModel
import com.kikepb.club.presentation.clubs.info.ClubInfoCenterViewModel
import com.kikepb.club.presentation.clubs.list.ClubTeamsListViewModel
import com.kikepb.club.presentation.clubs.member.ClubMemberDetailViewModel
import com.kikepb.club.presentation.clubs.shared.ClubRefreshNotifier
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val clubPresentationModule = module {
    singleOf(::ClubRefreshNotifier)
    viewModelOf(::ClubTeamsListViewModel)
    viewModelOf(::ClubInfoCenterViewModel)
    viewModelOf(::ClubDetailViewModel)
    viewModelOf(::ClubMemberDetailViewModel)
}
