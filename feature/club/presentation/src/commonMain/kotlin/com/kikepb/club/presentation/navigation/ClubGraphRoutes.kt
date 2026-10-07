package com.kikepb.club.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kikepb.club.presentation.bans.ClubBansRoot
import com.kikepb.club.presentation.detail.ClubDetailRoot
import com.kikepb.club.presentation.memberdetail.MemberDetailRoot
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubBansRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubDetailRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubMemberDetailRoute
import kotlinx.serialization.Serializable

sealed interface ClubGraphRoutes {
    @Serializable
    data object SetupGraph : ClubGraphRoutes

    @Serializable
    data object ClubsListRoute : ClubGraphRoutes

    @Serializable
    data object JoinClubRoute : ClubGraphRoutes

    @Serializable
    data object CreateClubRoute : ClubGraphRoutes

    /** [logoUploadFailed]: the club was just created but its logo could not be uploaded (AC-003-03). */
    @Serializable
    data class ClubDetailRoute(val clubId: String, val logoUploadFailed: Boolean = false) : ClubGraphRoutes

    @Serializable
    data class ClubMemberDetailRoute(val clubId: String, val memberId: String) : ClubGraphRoutes

    @Serializable
    data class ClubBansRoute(val clubId: String) : ClubGraphRoutes
}

fun NavGraphBuilder.clubGraph(navController: NavController) {
    composable<ClubDetailRoute> {
        ClubDetailRoot(
            onBackClick = { navController.navigateUp() },
            onMemberClick = { clubId, memberId ->
                navController.navigate(route = ClubMemberDetailRoute(clubId = clubId, memberId = memberId))
            },
            onOpenBans = { clubId -> navController.navigate(route = ClubBansRoute(clubId = clubId)) },
            onLeftClub = { navController.navigateUp() }
        )
    }
    composable<ClubMemberDetailRoute> {
        MemberDetailRoot(onBackClick = { navController.navigateUp() })
    }
    composable<ClubBansRoute> {
        ClubBansRoot(onBackClick = { navController.navigateUp() })
    }
}
