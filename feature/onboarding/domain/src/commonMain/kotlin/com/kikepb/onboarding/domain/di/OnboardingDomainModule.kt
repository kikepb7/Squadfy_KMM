package com.kikepb.onboarding.domain.di

import com.kikepb.onboarding.domain.usecase.GetHasSeenOnboardingUseCase
import com.kikepb.onboarding.domain.usecase.SetHasSeenOnboardingUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val onboardingDomainModule = module {
    factoryOf(::GetHasSeenOnboardingUseCase)
    factoryOf(::SetHasSeenOnboardingUseCase)
}
