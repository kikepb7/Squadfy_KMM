package org.kikepb.squadfy.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Graph
import com.kikepb.auth.presentation.navigation.authGraph
import com.kikepb.chat.presentation.chat_list.ChatListRoute
import com.kikepb.chat.presentation.chat_list.ChatListScreenRoot

@Composable
fun NavigationRoot(navController: NavHostController) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Graph
    ) {
        authGraph(
            navController = navController,
            onLoginSuccess = {
                navController.navigate(ChatListRoute) {
                    popUpTo(Graph) {
                        inclusive = true
                    }
                }
            }
        )
        composable<ChatListRoute> {
            ChatListScreenRoot()
        }
    }
}