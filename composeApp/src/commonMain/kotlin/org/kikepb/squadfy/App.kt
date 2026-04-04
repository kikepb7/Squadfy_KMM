package org.kikepb.squadfy

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.AuthGraph
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.presentation.util.ObserveAsEvents
import com.kikepb.globalPosition.presentation.navigation.GlobalPositionGraphRoutes.GlobalPositionGraph
import com.kikepb.onboarding.presentation.navigation.OnboardingGraphRoutes.OnboardingGraph
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kikepb.squadfy.navigation.DeepLinkListener
import org.kikepb.squadfy.navigation.NavigationRoot
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(
    onAuthenticationChecked: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = state.isCheckingAuth) {
        if (!state.isCheckingAuth) onAuthenticationChecked()
    }

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is MainEvent.OnSessionExpired -> {
                navController.navigate(route = AuthGraph) {
                    popUpTo(AuthGraph) {
                        inclusive = false
                    }
                }
            }
        }
    }

    SquadfyTheme {
        if (!state.isCheckingAuth) {
            val startDestination = when {
                !state.isLoggedIn -> AuthGraph
                !state.hasSeenOnboarding -> OnboardingGraph
                else -> GlobalPositionGraph
            }
            NavigationRoot(
                navController = navController,
                startDestination = startDestination,
                hasSeenOnboarding = state.hasSeenOnboarding
            )
            DeepLinkListener(navController = navController)
        }
    }
}