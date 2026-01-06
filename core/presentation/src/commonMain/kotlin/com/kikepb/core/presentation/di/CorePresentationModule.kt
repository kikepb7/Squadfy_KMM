package com.kikepb.core.presentation.di

import com.kikepb.core.presentation.util.ScopedStoreRegistryViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val corePresentationModule = module {
    singleOf(::ScopedStoreRegistryViewModel)
}