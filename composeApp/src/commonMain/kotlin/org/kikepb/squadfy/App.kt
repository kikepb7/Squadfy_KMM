package org.kikepb.squadfy

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.AuthGraph
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kikepb.squadfy.MainEvent.NavigateAfterLogin
import org.kikepb.squadfy.MainEvent.OnSessionExpired
import org.kikepb.squadfy.navigation.DeepLinkListener
import org.kikepb.squadfy.navigation.NavigationRoot
import org.kikepb.squadfy.utils.toRoute
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
            OnSessionExpired -> {
                navController.navigate(route = AuthGraph) {
                    popUpTo(AuthGraph) { inclusive = false }
                }
            }
            is NavigateAfterLogin -> {
                navController.navigate(route = event.destination.toRoute()) {
                    popUpTo(route = AuthGraph) { inclusive = true }
                }
            }
        }
    }

    SquadfyTheme {
        if (!state.isCheckingAuth) {
            NavigationRoot(
                navController = navController,
                startDestination = state.startDestination.toRoute(),
                onLoginSuccess = viewModel::onLoginCompleted
            )
            DeepLinkListener(navController = navController)
        }
    }
}
