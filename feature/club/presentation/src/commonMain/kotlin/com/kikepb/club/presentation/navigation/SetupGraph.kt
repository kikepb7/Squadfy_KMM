package com.kikepb.club.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kikepb.club.presentation.create.CreateClubRoot
import com.kikepb.club.presentation.join.JoinClubRoot
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.CreateClubRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.JoinClubRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.SetupGraph
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.SetupHome
import com.kikepb.club.presentation.setup.SetupRoot

fun NavGraphBuilder.setupGraph(
    navController: NavController,
    onSetupComplete: () -> Unit
) {
    navigation<SetupGraph>(
        startDestination = SetupHome
    ) {
        composable<SetupHome> {
            SetupRoot(
                onJoinClub = { navController.navigate(JoinClubRoute) },
                onCreateClub = { navController.navigate(CreateClubRoute) }
            )
        }

        composable<JoinClubRoute> {
            JoinClubRoot(
                onBackClick = { navController.navigateUp() },
                onSuccess = onSetupComplete
            )
        }

        composable<CreateClubRoute> {
            CreateClubRoot(
                onBackClick = { navController.navigateUp() },
                onSuccess = onSetupComplete
            )
        }
    }
}
