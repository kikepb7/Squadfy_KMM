package com.kikepb.club.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kikepb.club.presentation.clubs.detail.ClubDetailRoot
import com.kikepb.club.presentation.clubs.info.ClubInfoCenterRoot
import com.kikepb.club.presentation.clubs.list.ClubTeamsListRoot
import com.kikepb.club.presentation.clubs.member.ClubMemberDetailRoot
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubDetailRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubInfoCenterRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubMemberDetailRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubTeamsRoute
import kotlinx.serialization.Serializable

sealed interface ClubGraphRoutes {
    @Serializable
    data object ClubGraph : ClubGraphRoutes

    @Serializable
    data object ClubTeamsRoute : ClubGraphRoutes

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
    navigation<ClubGraphRoutes.ClubGraph>(
        startDestination = ClubTeamsRoute
    ) {
        composable<ClubTeamsRoute> {
            ClubTeamsListRoot(
                onClubClick = { clubId ->
                    navController.navigate(ClubDetailRoute(clubId = clubId))
                }
            )
        }

        composable<ClubInfoCenterRoute> {
            ClubInfoCenterRoot(
                onNavigateToTeams = {
                    navController.navigate(ClubTeamsRoute) {
                        popUpTo(ClubTeamsRoute) {
                            inclusive = false
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable<ClubDetailRoute> {
            ClubDetailRoot(
                onBackClick = { navController.navigateUp() },
                onMemberClick = { clubId, memberId ->
                    navController.navigate(
                        ClubMemberDetailRoute(
                            clubId = clubId,
                            memberId = memberId
                        )
                    )
                }
            )
        }

        composable<ClubMemberDetailRoute> {
            ClubMemberDetailRoot(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}
