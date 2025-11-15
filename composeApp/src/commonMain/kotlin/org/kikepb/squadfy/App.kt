package org.kikepb.squadfy

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Graph
import com.kikepb.chat.presentation.chat_list.ChatListRoute
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.presentation.util.ObserveAsEvents
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
    DeepLinkListener(navController = navController)

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = state.isCheckingAuth) {
        if (!state.isCheckingAuth) onAuthenticationChecked()
    }

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is MainEvent.OnSessionExpired -> {
                navController.navigate(route = Graph) {
                    popUpTo(Graph) {
                        inclusive = false
                    }
                }
            }
        }
    }

    SquadfyTheme {
        if (!state.isCheckingAuth) {
            NavigationRoot(
                navController = navController,
                startDestination = if (state.isLoggedIn) ChatListRoute else Graph
            )
        }
    }
}