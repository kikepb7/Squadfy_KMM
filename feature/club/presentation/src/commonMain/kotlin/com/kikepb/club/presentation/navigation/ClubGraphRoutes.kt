package com.kikepb.club.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kikepb.club.presentation.detail.ClubDetailRoot
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubDetailRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubMemberDetailRoute
import kotlinx.serialization.Serializable

sealed interface ClubGraphRoutes {
    @Serializable
    data object SetupGraph : ClubGraphRoutes

    @Serializable
    data object SetupHome : ClubGraphRoutes

    @Serializable
    data object JoinClubRoute : ClubGraphRoutes

    @Serializable
    data object CreateClubRoute : ClubGraphRoutes

    @Serializable
    data class ClubDetailRoute(val clubId: String) : ClubGraphRoutes

    @Serializable
    data class ClubMemberDetailRoute(
        val clubId: String,
        val memberId: String
    ) : ClubGraphRoutes
}

fun NavGraphBuilder.clubGraph(navController: NavController) {
    composable<ClubDetailRoute> {
        ClubDetailRoot(
            onBackClick = { navController.navigateUp() },
            onMemberClick = { clubId, memberId ->
                navController.navigate(
                    route = ClubMemberDetailRoute(clubId = clubId, memberId = memberId)
                )
            }
        )
    }
}
