package com.kikepb.global_position.presentation.di

import com.kikepb.globalPosition.presentation.GlobalPositionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val globalPositionPresentationModule = module {
    viewModelOf(::GlobalPositionViewModel)
}
