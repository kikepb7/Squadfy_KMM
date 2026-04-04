package com.kikepb.onboarding.domain.usecase

import com.kikepb.onboarding.domain.repository.OnboardingRepository

class SetHasSeenOnboardingUseCase(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke() = repository.setOnboardingAsSeen()
}
