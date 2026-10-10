package org.kikepb.squadfy.navigation.bottomBar

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.vector
import androidx.navigation.NavDestination
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import squadfy_app.composeapp.generated.resources.Res
import squadfy_app.composeapp.generated.resources.tab_chat
import squadfy_app.composeapp.generated.resources.tab_clubs
import squadfy_app.composeapp.generated.resources.tab_home
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.kikepb.chat.presentation.navigation.ChatGraphRoutes
import com.kikepb.club.presentation.navigation.ClubGraphRoutes
import com.kikepb.globalPosition.presentation.navigation.GlobalPositionGraphRoutes

sealed class BottomBarItem {
    abstract val title: StringResource
    abstract val icon: @Composable () -> Unit
    abstract val navigateTo: Any
    abstract fun isSelected(destination: NavDestination?): Boolean

    data object GlobalPosition : BottomBarItem() {
        override val title = Res.string.tab_home
        override val navigateTo: Any = GlobalPositionGraphRoutes.GlobalPositionGraph
        override val icon: @Composable () -> Unit = {
            Icon(imageVector = TriIcon.Home.vector, contentDescription = stringResource(title))
        }
        override fun isSelected(destination: NavDestination?) =
            destination?.hierarchy?.any { it.hasRoute(GlobalPositionGraphRoutes.GlobalPositionGraph::class) } == true
    }

    data object Setup : BottomBarItem() {
        override val title = Res.string.tab_clubs
        override val navigateTo: Any = ClubGraphRoutes.SetupGraph
        override val icon: @Composable () -> Unit = {
            Icon(imageVector = TriIcon.Clubs.vector, contentDescription = stringResource(title))
        }
        // Spec 018: every club screen (detail, match, schedule…) belongs to the Clubs tab
        override fun isSelected(destination: NavDestination?) =
            destination?.hierarchy?.any { it.hasRoute(ClubGraphRoutes.SetupGraph::class) } == true ||
                CLUB_ROUTES.any { destination?.hasRoute(it) == true }

        private val CLUB_ROUTES = listOf(
            ClubGraphRoutes.ClubDetailRoute::class,
            ClubGraphRoutes.ClubMemberDetailRoute::class,
            ClubGraphRoutes.ClubBansRoute::class,
            ClubGraphRoutes.ClubScheduleRoute::class,
            ClubGraphRoutes.ClubAbsencesRoute::class,
            ClubGraphRoutes.MatchDetailRoute::class
        )
    }

    data object Chat : BottomBarItem() {
        override val title = Res.string.tab_chat
        override val navigateTo: Any = ChatGraphRoutes.ChatGraph
        override val icon: @Composable () -> Unit = {
            Icon(imageVector = TriIcon.Chat.vector, contentDescription = stringResource(title))
        }
        override fun isSelected(destination: NavDestination?) =
            destination?.hierarchy?.any { it.hasRoute(ChatGraphRoutes.ChatGraph::class) } == true
    }
}