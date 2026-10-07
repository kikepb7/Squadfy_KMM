package com.kikepb.club.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.kikepb.club.presentation.bans.ClubBansRoot
import com.kikepb.club.presentation.detail.ClubDetailRoot
import com.kikepb.club.presentation.memberdetail.MemberDetailRoot
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubBansRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubDetailRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubMemberDetailRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubScheduleRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.MatchDetailRoute
import com.kikepb.club.presentation.navigation.ClubGraphRoutes.ClubAbsencesRoute
import com.kikepb.club.presentation.absences.AbsencesRoot
import com.kikepb.club.presentation.match.MatchDetailRoot
import com.kikepb.club.presentation.schedule.ScheduleRoot
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

    @Serializable
    data class ClubScheduleRoute(val clubId: String) : ClubGraphRoutes

    @Serializable
    data class ClubAbsencesRoute(val clubId: String) : ClubGraphRoutes

    @Serializable
    data class MatchDetailRoute(val clubId: String, val matchId: String) : ClubGraphRoutes
}

fun NavGraphBuilder.clubGraph(navController: NavController) {
    // Push deep links (spec 009, AC-009-03): the club opens on its Match tab
    composable<ClubDetailRoute>(deepLinks = listOf(navDeepLink { uriPattern = "squadfy://club/{clubId}/announcement" })) {
        ClubDetailRoot(
            onBackClick = { navController.navigateUp() },
            onMemberClick = { clubId, memberId ->
                navController.navigate(route = ClubMemberDetailRoute(clubId = clubId, memberId = memberId))
            },
            onOpenBans = { clubId -> navController.navigate(route = ClubBansRoute(clubId = clubId)) },
            onOpenSchedule = { clubId -> navController.navigate(route = ClubScheduleRoute(clubId = clubId)) },
            onOpenMatch = { clubId, matchId -> navController.navigate(route = MatchDetailRoute(clubId = clubId, matchId = matchId)) },
            onOpenAbsences = { clubId -> navController.navigate(route = ClubAbsencesRoute(clubId = clubId)) },
            onLeftClub = { navController.navigateUp() }
        )
    }
    composable<ClubMemberDetailRoute> {
        MemberDetailRoot(onBackClick = { navController.navigateUp() })
    }
    composable<ClubBansRoute> {
        ClubBansRoot(onBackClick = { navController.navigateUp() })
    }
    composable<ClubScheduleRoute> {
        ScheduleRoot(onBackClick = { navController.navigateUp() })
    }
    composable<ClubAbsencesRoute> {
        AbsencesRoot(onBackClick = { navController.navigateUp() })
    }
    composable<MatchDetailRoute>(deepLinks = listOf(navDeepLink { uriPattern = "squadfy://match/{matchId}?clubId={clubId}" })) {
        MatchDetailRoot(onBackClick = { navController.navigateUp() })
    }
}
