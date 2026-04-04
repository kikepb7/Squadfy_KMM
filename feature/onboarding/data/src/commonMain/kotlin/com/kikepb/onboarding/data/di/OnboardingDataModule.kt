package com.kikepb.onboarding.data.di

import com.kikepb.onboarding.data.repository.OnboardingRepositoryImpl
import com.kikepb.onboarding.domain.repository.OnboardingRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformOnboardingDataModule: Module
val onboardingDataModule = module {
    includes(platformOnboardingDataModule)
    singleOf(::OnboardingRepositoryImpl) bind OnboardingRepository::class
}
