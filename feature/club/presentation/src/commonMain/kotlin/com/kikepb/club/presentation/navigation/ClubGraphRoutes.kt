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
    data object ClubGraph : ClubGraphRoutes

    @Serializable
    data object ClubInfoCenterRoute : ClubGraphRoutes

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

//    composable<ClubTeamsRoute> {
//        ClubTeamsListRoot(
//            onClubClick = { clubId ->
//                navController.navigate(ClubDetailRoute(clubId = clubId))
//            }
//        )
//    }
//
//    composable<ClubInfoCenterRoute> {
//        ClubInfoCenterRoot(
//            onNavigateToTeams = {
//                navController.navigate(ClubTeamsRoute) {
//                    popUpTo(ClubTeamsRoute) { inclusive = false; saveState = true }
//                    launchSingleTop = true
//                    restoreState = true
//                }
//            }
//        )
//    }
//
//    composable<ClubMemberDetailRoute> {
//        ClubMemberDetailRoot(
//            onBackClick = { navController.navigateUp() }
//        )
//    }
//}
