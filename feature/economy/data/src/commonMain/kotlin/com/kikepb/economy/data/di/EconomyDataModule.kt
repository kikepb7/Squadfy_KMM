package com.kikepb.economy.data.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformEconomyDataModule: Module
val economyDataModule = module {
    includes(platformEconomyDataModule)
}
