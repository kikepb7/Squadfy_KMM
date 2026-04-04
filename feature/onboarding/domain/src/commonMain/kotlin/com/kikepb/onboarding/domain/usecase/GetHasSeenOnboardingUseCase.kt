package com.kikepb.onboarding.domain.usecase

import com.kikepb.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class GetHasSeenOnboardingUseCase(
    private val repository: OnboardingRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.hasSeenOnboarding()
}
