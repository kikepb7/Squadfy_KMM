package org.kikepb.squadfy.utils

import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.AuthGraph
import com.kikepb.globalPosition.presentation.navigation.GlobalPositionGraphRoutes.GlobalPositionGraph
import com.kikepb.onboarding.presentation.navigation.OnboardingGraphRoutes.OnboardingGraph
import org.kikepb.squadfy.StartDestination
import org.kikepb.squadfy.StartDestination.Auth
import org.kikepb.squadfy.StartDestination.Home
import org.kikepb.squadfy.StartDestination.Onboarding

fun StartDestination.toRoute(): Any = when (this) {
    Auth -> AuthGraph
    Onboarding -> OnboardingGraph
    Home -> GlobalPositionGraph
}