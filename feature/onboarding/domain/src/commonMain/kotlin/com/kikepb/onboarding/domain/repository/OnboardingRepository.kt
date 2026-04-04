package com.kikepb.onboarding.domain.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun hasSeenOnboarding(): Flow<Boolean>
    suspend fun setOnboardingAsSeen()
}
