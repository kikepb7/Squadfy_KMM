package com.kikepb.club.presentation.di

import com.kikepb.club.presentation.detail.ClubDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val clubPresentationModule = module {
    viewModelOf(::ClubDetailViewModel)
}
