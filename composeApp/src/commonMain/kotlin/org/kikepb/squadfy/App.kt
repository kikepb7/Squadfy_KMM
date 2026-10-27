package org.kikepb.squadfy

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.AuthGraph
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.presentation.util.ObserveAsEvents
import com.kikepb.globalPosition.presentation.navigation.GlobalPositionGraphRoutes.GlobalPositionGraph
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kikepb.squadfy.MainEvent.OnSessionExpired
import org.kikepb.squadfy.navigation.DeepLinkListener
import org.kikepb.squadfy.navigation.NavigationRoot
import org.kikepb.squadfy.di.onAppForeground
import org.kikepb.squadfy.update.AppUpdateRoot
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(
    onAuthenticationChecked: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Spec 019: back in the foreground, a flag switched in Remote Config arrives without a restart
    val koin = getKoin()
    LifecycleEventEffect(Lifecycle.Event.ON_START) { koin.onAppForeground() }

    LaunchedEffect(key1 = state.isCheckingAuth) {
        if (!state.isCheckingAuth) onAuthenticationChecked()
    }

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is OnSessionExpired -> {
                navController.navigate(route = AuthGraph) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    SquadfyTheme {
        if (!state.isCheckingAuth) {
            NavigationRoot(
                navController = navController,
                startDestination = if (state.isLoggedIn) GlobalPositionGraph else AuthGraph
            )
            DeepLinkListener(navController = navController)
            // Spec 021 APP-RN-P8: a new version, over any screen (signed in or not)
            AppUpdateRoot()
        }
    }
}