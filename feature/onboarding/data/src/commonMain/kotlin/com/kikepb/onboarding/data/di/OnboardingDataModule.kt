package com.kikepb.onboarding.data.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformOnboardingDataModule: Module
val onboardingDataModule = module {
    includes(platformOnboardingDataModule)
}
