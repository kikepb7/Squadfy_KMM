package com.kikepb.onboarding.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface OnboardingGraphRoutes {
    @Serializable
    data object OnboardingGraph : OnboardingGraphRoutes

    @Serializable
    data object Onboarding : OnboardingGraphRoutes
}
