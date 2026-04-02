package com.kikepb.club.data.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformClubDataModule: Module
val clubDataModule = module {
    includes(platformClubDataModule)
}
