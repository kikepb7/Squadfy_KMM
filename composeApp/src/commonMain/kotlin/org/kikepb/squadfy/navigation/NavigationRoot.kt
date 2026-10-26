package org.kikepb.squadfy.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import com.kikepb.core.designsystem.theme.extended
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.kikepb.chat.presentation.profile.ProfileRoot
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.core.presentation.util.DialogSheetScopedViewModel
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.AuthGraph
import com.kikepb.auth.presentation.navigation.authGraph
import com.kikepb.chat.presentation.navigation.chatGraph
import com.kikepb.club.presentation.navigation.ClubGraphRoutes
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubDetailRoute
import com.kikepb.club.presentation.navigation.clubGraph
import com.kikepb.club.presentation.navigation.setupGraph
import com.kikepb.core.designsystem.components.navigation.SquadfyBottomBar
import com.kikepb.core.designsystem.components.navigation.SquadfyBottomBarItemModel
import com.kikepb.core.domain.featureflag.AppEnvironment
import com.kikepb.globalPosition.presentation.navigation.GlobalPositionGraphRoutes.GlobalPositionGraph
import com.kikepb.globalPosition.presentation.navigation.globalPositionGraph
import org.kikepb.squadfy.debug.FeatureFlagsRoot
import org.kikepb.squadfy.debug.FeatureFlagsRoute
import org.kikepb.squadfy.navigation.bottomBar.BottomBarItem.Chat
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import com.kikepb.core.domain.analytics.AnalyticsTracker
import org.kikepb.squadfy.navigation.bottomBar.BottomBarItem.GlobalPosition
import org.kikepb.squadfy.navigation.bottomBar.BottomBarItem.Setup

@Composable
fun NavigationRoot(navController: NavHostController, startDestination: Any) {
    // Feature flags debug screen only exists in PRE builds (spec 013, AC-013-06)
    val isPreEnvironment = koinInject<AppEnvironment>() == AppEnvironment.PRE
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // D-13: the chat tab follows the CHAT flag (hidden on iOS in PRO)
    val featureFlags = koinInject<FeatureFlags>()
    val isChatEnabled by remember { featureFlags.observe(FeatureFlag.CHAT) }.collectAsState(initial = featureFlags.isEnabled(FeatureFlag.CHAT))
    val bottomBarItems = if (isChatEnabled) listOf(GlobalPosition, Setup, Chat) else listOf(GlobalPosition, Setup)
    // Profile (account, privacy, crash reports, sign out) opens from the Home gear on every platform
    var isProfileVisible by rememberSaveable { mutableStateOf(false) }
    // AC-019-18: screen_view per destination; Profile is an overlay, not a destination, so it is logged here
    val analyticsTracker = koinInject<AnalyticsTracker>()
    TrackScreenViews(navController = navController, tracker = analyticsTracker)
    LaunchedEffect(isProfileVisible) { if (isProfileVisible) analyticsTracker.logScreenView(PROFILE_SCREEN_NAME) }
    // Spec 018: the bar is on every signed-in screen (not on the auth flow)
    val isAuthDestination = currentDestination?.hierarchy?.any { it.hasRoute(AuthGraph::class) } == true
    // The draw result is a full-screen moment (screen 15), without the bar
    val isFullScreenMoment = currentDestination?.hasRoute(ClubGraphRoutes.MatchDrawRoute::class) == true
    val showBottomBar = currentDestination != null && !isAuthDestination && !isFullScreenMoment
    val selectedIndex = bottomBarItems.indexOfFirst { it.isSelected(destination = currentDestination) }.coerceAtLeast(minimumValue = 0)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        // Spec 017, edge to edge: each screen paints behind the system bars (its top bar behind the status bar) and
        // pads them itself, so the status bar always matches the screen. Only the bottom bar's space is reserved here.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                SquadfyBottomBar(
                    items = bottomBarItems.map { item ->
                        SquadfyBottomBarItemModel(label = stringResource(item.title), icon = item.icon)
                    },
                    selectedIndex = selectedIndex,
                    onItemClick = { index ->
                        // From a detail screen, a tab goes back to its root instead of restoring the detail
                        val isTabRoot = currentDestination?.hierarchy?.any { destination ->
                            bottomBarItems.any { destination.hasRoute(it.navigateTo::class) }
                        } == true
                        if (!isTabRoot) navController.popBackStack(navController.graph.findStartDestination().id, inclusive = false)
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
            modifier = Modifier.fillMaxSize().padding(paddingValues).consumeWindowInsets(paddingValues)
        ) {
            authGraph(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(route = GlobalPositionGraph) {
                        popUpTo(route = AuthGraph) {
                            inclusive = true
                        }
                    }
                }
            )
            globalPositionGraph(
                onNavigateToClub = { clubId ->
                    navController.navigate(ClubDetailRoute(clubId = clubId))
                },
                onNavigateToSettings = { isProfileVisible = true },
                onNavigateToMatchResult = { clubId, matchId ->
                    navController.navigate(ClubGraphRoutes.MatchResultRoute(clubId = clubId, matchId = matchId))
                },
                onNavigateToClubs = {
                    navController.navigate(ClubGraphRoutes.SetupGraph) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
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
            setupGraph(navController = navController)
            clubGraph(navController = navController)
            if (isPreEnvironment) {
                composable<FeatureFlagsRoute> {
                    FeatureFlagsRoot(onBackClick = { navController.navigateUp() })
                }
            }
        }
    }

    DialogSheetScopedViewModel(visible = isProfileVisible) {
        val goToAuth = {
            isProfileVisible = false
            navController.navigate(route = AuthGraph) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            }
        }
        ProfileRoot(
            onDismiss = { isProfileVisible = false },
            onAccountDeleted = goToAuth,
            onSignedOut = goToAuth,
            onFeatureFlagsClick = if (isPreEnvironment) {
                {
                    isProfileVisible = false
                    navController.navigate(FeatureFlagsRoute)
                }
            } else null
        )
    }
}

private const val PROFILE_SCREEN_NAME = "Profile"
