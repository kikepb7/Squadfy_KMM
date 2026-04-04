package com.kikepb.onboarding.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kikepb.onboarding.presentation.OnboardingRoot
import com.kikepb.onboarding.presentation.navigation.OnboardingGraphRoutes.Onboarding
import com.kikepb.onboarding.presentation.navigation.OnboardingGraphRoutes.OnboardingGraph

fun NavGraphBuilder.onboardingGraph(onOnboardingFinished: () -> Unit) {
    navigation<OnboardingGraph>(startDestination = Onboarding) {
        composable<Onboarding> { OnboardingRoot(onFinished = onOnboardingFinished) }
    }
}
