package org.kikepb.squadfy.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.AuthGraph
import com.kikepb.auth.presentation.navigation.authGraph
import com.kikepb.chat.presentation.navigation.chatGraph
import com.kikepb.core.designsystem.components.navigation.SquadfyBottomBar
import com.kikepb.core.designsystem.components.navigation.SquadfyBottomBarItemModel
import com.kikepb.globalPosition.presentation.navigation.GlobalPositionGraphRoutes.GlobalPositionGraph
import com.kikepb.globalPosition.presentation.navigation.globalPositionGraph
import com.kikepb.onboarding.presentation.navigation.OnboardingGraphRoutes.OnboardingGraph
import com.kikepb.onboarding.presentation.navigation.onboardingGraph
import org.kikepb.squadfy.navigation.bottomBar.BottomBarItem.Chat
import org.kikepb.squadfy.navigation.bottomBar.BottomBarItem.GlobalPosition

@Composable
fun NavigationRoot(
    navController: NavHostController,
    startDestination: Any,
    hasSeenOnboarding: Boolean
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentHasSeenOnboarding by rememberUpdatedState(hasSeenOnboarding)

    val bottomBarItems = listOf(GlobalPosition, Chat)
    val showBottomBar = bottomBarItems.any { it.isSelected(destination = currentDestination) }
    val selectedIndex = bottomBarItems.indexOfFirst { it.isSelected(destination = currentDestination) }.coerceAtLeast(minimumValue = 0)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                SquadfyBottomBar(
                    items = bottomBarItems.map { item ->
                        SquadfyBottomBarItemModel(label = item.title, icon = item.icon)
                    },
                    selectedIndex = selectedIndex,
                    onItemClick = { index ->
                        navController.navigate(bottomBarItems[index].navigateTo) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            authGraph(
                navController = navController,
                onLoginSuccess = {
                    val destination = if (currentHasSeenOnboarding) GlobalPositionGraph else OnboardingGraph
                    navController.navigate(route = destination) {
                        popUpTo(route = AuthGraph) {
                            inclusive = true
                        }
                    }
                }
            )
            onboardingGraph(
                onOnboardingFinished = {
                    navController.navigate(route = GlobalPositionGraph) {
                        popUpTo(route = OnboardingGraph) {
                            inclusive = true
                        }
                    }
                }
            )
            globalPositionGraph()
            chatGraph(
                navController = navController,
                onLogout = {
                    navController.navigate(route = AuthGraph) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
