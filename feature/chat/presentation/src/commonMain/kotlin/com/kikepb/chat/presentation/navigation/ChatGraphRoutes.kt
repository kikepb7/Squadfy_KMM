package com.kikepb.chat.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAdaptiveLayout
import com.kikepb.chat.presentation.navigation.ChatGraphRoutes.ChatListDetailRoute
import com.kikepb.chat.presentation.navigation.ChatGraphRoutes.ChatGraph
import kotlinx.serialization.Serializable

sealed interface ChatGraphRoutes {
    @Serializable
    data object ChatGraph : ChatGraphRoutes

    @Serializable
    data class ChatListDetailRoute(val chatId: String? = null): ChatGraphRoutes
}

fun NavGraphBuilder.chatGraph(navController: NavController) {
    navigation<ChatGraph>(
        startDestination = ChatListDetailRoute(chatId = null)
    ) {
        composable<ChatListDetailRoute>(
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "squadfy://chat_details/{chatId}"
                }
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<ChatListDetailRoute>()
            ChatListDetailAdaptiveLayout(
                initialChatId = route.chatId,
                onLogout = {
                    // TODO --> Implement logout navigation
                }
            )
        }
    }
}