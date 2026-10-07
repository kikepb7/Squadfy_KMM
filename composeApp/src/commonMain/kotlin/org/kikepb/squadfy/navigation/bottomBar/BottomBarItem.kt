package org.kikepb.squadfy.navigation.bottomBar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
            Icon(imageVector = Icons.Default.Home, contentDescription = stringResource(title))
        }
        override fun isSelected(destination: NavDestination?) =
            destination?.hierarchy?.any { it.hasRoute(GlobalPositionGraphRoutes.GlobalPositionGraph::class) } == true
    }

    data object Setup : BottomBarItem() {
        override val title = Res.string.tab_clubs
        override val navigateTo: Any = ClubGraphRoutes.SetupGraph
        override val icon: @Composable () -> Unit = {
            Icon(imageVector = Icons.Outlined.Groups, contentDescription = stringResource(title))
        }
        override fun isSelected(destination: NavDestination?) =
            destination?.hierarchy?.any { it.hasRoute(ClubGraphRoutes.SetupGraph::class) } == true
    }

    data object Chat : BottomBarItem() {
        override val title = Res.string.tab_chat
        override val navigateTo: Any = ChatGraphRoutes.ChatGraph
        override val icon: @Composable () -> Unit = {
            Icon(imageVector = Icons.Outlined.MailOutline, contentDescription = stringResource(title))
        }
        override fun isSelected(destination: NavDestination?) =
            destination?.hierarchy?.any { it.hasRoute(ChatGraphRoutes.ChatGraph::class) } == true
    }
}