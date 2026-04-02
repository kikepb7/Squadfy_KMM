package com.kikepb.global_position.data.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformGlobalPositionDataModule: Module
val globalPositionDataModule = module {
    includes(platformGlobalPositionDataModule)
}