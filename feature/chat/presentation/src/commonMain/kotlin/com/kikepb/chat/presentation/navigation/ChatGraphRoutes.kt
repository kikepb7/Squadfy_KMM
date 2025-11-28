package com.kikepb.chat.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAdaptiveLayout
import com.kikepb.chat.presentation.navigation.ChatGraphRoutes.ChatListDetailRoute
import com.kikepb.chat.presentation.navigation.ChatGraphRoutes.ChatGraph
import kotlinx.serialization.Serializable

sealed interface ChatGraphRoutes {
    @Serializable
    data object ChatGraph : ChatGraphRoutes

    @Serializable
    data object ChatListDetailRoute: ChatGraphRoutes
}

fun NavGraphBuilder.chatGraph(
    navController: NavController
) {
    navigation<ChatGraph>(
        startDestination = ChatListDetailRoute
    ) {
        composable<ChatListDetailRoute> {
            ChatListDetailAdaptiveLayout(
                onLogout = {
                    // TODO --> Implement logout navigation
                }
            )
        }
    }
}