package com.kikepb.club.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kikepb.club.presentation.clubs.ClubsListRoot
import com.kikepb.club.presentation.create.CreateClubRoot
import com.kikepb.club.presentation.join.JoinClubRoot
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubDetailRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubsListRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.CreateClubRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.JoinClubRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.SetupGraph

/** "Clubs" tab: my clubs plus the join/create flows (spec 003). */
fun NavGraphBuilder.setupGraph(navController: NavController) {
    navigation<SetupGraph>(startDestination = ClubsListRoute) {
        composable<ClubsListRoute> {
            ClubsListRoot(
                onClubClick = { clubId -> navController.navigate(ClubDetailRoute(clubId = clubId)) },
                onJoinClub = { navController.navigate(JoinClubRoute) },
                onCreateClub = { navController.navigate(CreateClubRoute) }
            )
        }
        composable<JoinClubRoute> {
            JoinClubRoot(
                onBackClick = { navController.navigateUp() },
                onSuccess = { clubId -> navController.openNewClub(ClubDetailRoute(clubId = clubId)) }
            )
        }
        composable<CreateClubRoute> {
            CreateClubRoot(
                onBackClick = { navController.navigateUp() },
                onSuccess = { clubId, logoUploadFailed ->
                    navController.openNewClub(ClubDetailRoute(clubId = clubId, logoUploadFailed = logoUploadFailed))
                }
            )
        }
    }
}

/** APP-RN-12: after creating or joining, open the club and drop the form from the back stack. */
private fun NavController.openNewClub(route: ClubDetailRoute) {
    navigate(route) {
        popUpTo(ClubsListRoute)
    }
}
