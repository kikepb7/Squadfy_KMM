package org.kikepb.squadfy.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.AuthGraph
import com.kikepb.auth.presentation.navigation.authGraph
import com.kikepb.chat.presentation.navigation.ChatGraphRoutes.ChatGraph
import com.kikepb.chat.presentation.navigation.chatGraph

@Composable
fun NavigationRoot(navController: NavHostController, startDestination: Any) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authGraph(
            navController = navController,
            onLoginSuccess = {
                navController.navigate(route = ChatGraph) {
                    popUpTo(route = AuthGraph) {
                        inclusive = true
                    }
                }
            }
        )
        chatGraph(
            navController = navController,
            onLogout = {
                navController.navigate(route = AuthGraph) {
                    popUpTo(route = ChatGraph) {
                        inclusive = true
                    }
                }
            }
        )
    }
}