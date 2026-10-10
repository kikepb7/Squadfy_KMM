package com.kikepb.club.presentation.preview

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.presentation.absences.AbsencesScreen
import com.kikepb.club.presentation.announcement.AnnouncementContent
import com.kikepb.club.presentation.announcement.AnnouncementState
import com.kikepb.club.presentation.bans.ClubBansScreen
import com.kikepb.club.presentation.clubs.ClubsListScreen
import com.kikepb.club.presentation.create.CreateClubScreen
import com.kikepb.club.presentation.create.CreateClubState
import com.kikepb.club.presentation.detail.ClubDetailScreen
import com.kikepb.club.presentation.detail.components.SquadfyClubDetailMembersTab
import com.kikepb.club.presentation.detail.model.ClubDetailTabModel
import com.kikepb.club.presentation.join.JoinClubScreen
import com.kikepb.club.presentation.join.JoinClubState
import com.kikepb.club.presentation.match.MatchDetailScreen
import com.kikepb.club.presentation.match.MatchDrawScreen
import com.kikepb.club.presentation.match.MatchPitchScreen
import com.kikepb.club.presentation.match.MatchResultScreen
import com.kikepb.club.presentation.memberdetail.MemberDetailScreen
import com.kikepb.club.presentation.memberdetail.MemberStatsContent
import com.kikepb.club.presentation.schedule.ScheduleScreen
import com.kikepb.club.presentation.settings.ClubSettingsContent
import com.kikepb.club.presentation.standings.StandingsContent
import com.kikepb.club.presentation.teams.TeamsContent
import com.kikepb.club.presentation.teams.TeamsState
import com.kikepb.club.presentation.teams.TeamsView
import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import org.jetbrains.compose.ui.tooling.preview.Preview

// Spec 018: every club screen in Tiza and Noche, with the data of the mockups (ClubSamples). Names follow the
// kit's screen numbers (pantallas/design/{tiza|noche}/es/NN-*.png).

/** The club screen with one tab drawn from sample state (the real tabs use their own ViewModels). */
@Composable
private fun ClubDetail(
    tab: ClubDetailTabModel,
    announcement: AnnouncementState = ClubSamples.announcement(),
    teams: TeamsState = ClubSamples.teams
) {
    ClubDetailScreen(
        state = ClubSamples.clubDetail,
        onAction = {},
        onBackClick = {},
        onMemberClick = { _, _ -> },
        onOpenBans = {},
        onOpenSchedule = {},
        onOpenMatch = { _, _ -> },
        onLeftClub = {},
        snackbarHostState = remember { SnackbarHostState() },
        initialTab = tab,
        tabContent = { selected ->
            when (selected) {
                ClubDetailTabModel.MATCH -> AnnouncementContent(state = announcement, onAction = {}, onOpenSchedule = {}, onOpenMatch = {})
                ClubDetailTabModel.TEAMS -> TeamsContent(state = teams, onAction = {}, onOpenMatch = {})
                ClubDetailTabModel.CLASSIFICATION -> StandingsContent(club = ClubSamples.club, state = ClubSamples.standings, onAction = {}, onMemberClick = {})
                ClubDetailTabModel.MEMBERS -> SquadfyClubDetailMembersTab(members = ClubSamples.members, onMemberClick = {})
                ClubDetailTabModel.SETTINGS -> ClubSettingsContent(
                    club = ClubSamples.club, myMembership = ClubSamples.alvaro, state = ClubSamples.clubSettings, onAction = {},
                    onCopyCode = {}, onShareCode = {}, onChangeLogo = {}, onOpenBans = {}, onOpenSchedule = {}, onOpenAbsences = {}
                )
            }
        }
    )
}

@Preview(name = "04 Club · Partido · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchTiza() = ScreenPreview(darkTheme = false) { ClubDetail(ClubDetailTabModel.MATCH) }

@Preview(name = "04 Club · Partido · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchNoche() = ScreenPreview(darkTheme = true) { ClubDetail(ClubDetailTabModel.MATCH) }

@Preview(name = "04b Partido · convocado · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchConfirmedTiza() = ScreenPreview(darkTheme = false) { ClubDetail(ClubDetailTabModel.MATCH, ClubSamples.announcement(MyEnrollmentStatus.CONFIRMED)) }

@Preview(name = "04b Partido · convocado · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchConfirmedNoche() = ScreenPreview(darkTheme = true) { ClubDetail(ClubDetailTabModel.MATCH, ClubSamples.announcement(MyEnrollmentStatus.CONFIRMED)) }

@Preview(name = "04c Partido · en espera · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchWaitlistTiza() = ScreenPreview(darkTheme = false) { ClubDetail(ClubDetailTabModel.MATCH, ClubSamples.announcement(MyEnrollmentStatus.WAITLISTED)) }

@Preview(name = "04c Partido · en espera · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchWaitlistNoche() = ScreenPreview(darkTheme = true) { ClubDetail(ClubDetailTabModel.MATCH, ClubSamples.announcement(MyEnrollmentStatus.WAITLISTED)) }

@Preview(name = "04d Partido · cerrada · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchClosedTiza() = ScreenPreview(darkTheme = false) { ClubDetail(ClubDetailTabModel.MATCH, ClubSamples.announcementClosed) }

@Preview(name = "04d Partido · cerrada · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchClosedNoche() = ScreenPreview(darkTheme = true) { ClubDetail(ClubDetailTabModel.MATCH, ClubSamples.announcementClosed) }

@Preview(name = "05 Club · Equipos (campo) · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubTeamsPitchTiza() = ScreenPreview(darkTheme = false) { ClubDetail(ClubDetailTabModel.TEAMS) }

@Preview(name = "05 Club · Equipos (campo) · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubTeamsPitchNoche() = ScreenPreview(darkTheme = true) { ClubDetail(ClubDetailTabModel.TEAMS) }

@Preview(name = "05b Club · Equipos (lista) · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubTeamsListTiza() = ScreenPreview(darkTheme = false) { ClubDetail(ClubDetailTabModel.TEAMS, teams = ClubSamples.teams.copy(view = TeamsView.LIST)) }

@Preview(name = "05b Club · Equipos (lista) · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubTeamsListNoche() = ScreenPreview(darkTheme = true) { ClubDetail(ClubDetailTabModel.TEAMS, teams = ClubSamples.teams.copy(view = TeamsView.LIST)) }

@Preview(name = "06 Campo · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun PitchTiza() = ScreenPreview(darkTheme = false) { MatchPitchScreen(state = ClubSamples.matchDetail(MatchStatus.SCHEDULED), onBackClick = {}) }

@Preview(name = "06 Campo · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun PitchNoche() = ScreenPreview(darkTheme = true) { MatchPitchScreen(state = ClubSamples.matchDetail(MatchStatus.SCHEDULED), onBackClick = {}) }

@Preview(name = "07 Acta · por cerrar · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ReportOpenTiza() = ScreenPreview(darkTheme = false) { MatchDetailScreen(state = ClubSamples.matchDetail(MatchStatus.SCHEDULED).copy(match = ClubSamples.match(MatchStatus.COMPLETED).copy(status = MatchStatus.SCHEDULED, ratingChanges = emptyMap())), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }, onOpenPitch = {}) }

@Preview(name = "07 Acta · por cerrar · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ReportOpenNoche() = ScreenPreview(darkTheme = true) { MatchDetailScreen(state = ClubSamples.matchDetail(MatchStatus.SCHEDULED).copy(match = ClubSamples.match(MatchStatus.COMPLETED).copy(status = MatchStatus.SCHEDULED, ratingChanges = emptyMap())), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }, onOpenPitch = {}) }

@Preview(name = "07b Acta · cerrado · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ReportClosedTiza() = ScreenPreview(darkTheme = false) { MatchDetailScreen(state = ClubSamples.matchDetail(MatchStatus.COMPLETED), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }, onOpenResult = {}) }

@Preview(name = "07b Acta · cerrado · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ReportClosedNoche() = ScreenPreview(darkTheme = true) { MatchDetailScreen(state = ClubSamples.matchDetail(MatchStatus.COMPLETED), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }, onOpenResult = {}) }

@Preview(name = "08 Club · Tabla · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubTableTiza() = ScreenPreview(darkTheme = false) { ClubDetail(ClubDetailTabModel.CLASSIFICATION) }

@Preview(name = "08 Club · Tabla · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubTableNoche() = ScreenPreview(darkTheme = true) { ClubDetail(ClubDetailTabModel.CLASSIFICATION) }

@Preview(name = "08b Club · Miembros · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMembersTiza() = ScreenPreview(darkTheme = false) { ClubDetail(ClubDetailTabModel.MEMBERS) }

@Preview(name = "08b Club · Miembros · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMembersNoche() = ScreenPreview(darkTheme = true) { ClubDetail(ClubDetailTabModel.MEMBERS) }

@Preview(name = "08c Club · Ajustes · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubSettingsTiza() = ScreenPreview(darkTheme = false) { ClubDetail(ClubDetailTabModel.SETTINGS) }

@Preview(name = "08c Club · Ajustes · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubSettingsNoche() = ScreenPreview(darkTheme = true) { ClubDetail(ClubDetailTabModel.SETTINGS) }

@Preview(name = "09 Ficha del jugador · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun PlayerCardTiza() = ScreenPreview(darkTheme = false) { MemberDetailScreen(state = ClubSamples.memberDetail, onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }, statsSection = { MemberStatsContent(member = it, state = ClubSamples.memberStats) }) }

@Preview(name = "09 Ficha del jugador · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun PlayerCardNoche() = ScreenPreview(darkTheme = true) { MemberDetailScreen(state = ClubSamples.memberDetail, onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }, statsSection = { MemberStatsContent(member = it, state = ClubSamples.memberStats) }) }

@Preview(name = "10 Unirse con código · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun JoinTiza() = ScreenPreview(darkTheme = false) { JoinClubScreen(state = JoinClubState(invitationCodeState = TextFieldState("K7Q2M9")), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }, onCreateClubClick = {}) }

@Preview(name = "10 Unirse con código · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun JoinNoche() = ScreenPreview(darkTheme = true) { JoinClubScreen(state = JoinClubState(invitationCodeState = TextFieldState("K7Q2M9")), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }, onCreateClubClick = {}) }

@Preview(name = "10b Crear club · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun CreateTiza() = ScreenPreview(darkTheme = false) { CreateClubScreen(state = CreateClubState(), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }, onPickLogo = {}) }

@Preview(name = "10b Crear club · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun CreateNoche() = ScreenPreview(darkTheme = true) { CreateClubScreen(state = CreateClubState(), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }, onPickLogo = {}) }

@Preview(name = "11 Horario semanal · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ScheduleTiza() = ScreenPreview(darkTheme = false) { ScheduleScreen(state = ClubSamples.schedule(), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "11 Horario semanal · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ScheduleNoche() = ScreenPreview(darkTheme = true) { ScheduleScreen(state = ClubSamples.schedule(), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "12 Ausencias · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun AbsencesTiza() = ScreenPreview(darkTheme = false) { AbsencesScreen(state = ClubSamples.absences, onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "12 Ausencias · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun AbsencesNoche() = ScreenPreview(darkTheme = true) { AbsencesScreen(state = ClubSamples.absences, onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "12b Vetados · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun BansTiza() = ScreenPreview(darkTheme = false) { ClubBansScreen(state = ClubSamples.bans, onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "12b Vetados · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun BansNoche() = ScreenPreview(darkTheme = true) { ClubBansScreen(state = ClubSamples.bans, onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "12c Mis clubes · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubsListTiza() = ScreenPreview(darkTheme = false) { ClubsListScreen(state = ClubSamples.clubsList, onAction = {}, onClubClick = {}, onJoinClub = {}, onCreateClub = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "12c Mis clubes · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubsListNoche() = ScreenPreview(darkTheme = true) { ClubsListScreen(state = ClubSamples.clubsList, onAction = {}, onClubClick = {}, onJoinClub = {}, onCreateClub = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "15 Sorteo completado · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun DrawTiza() = ScreenPreview(darkTheme = false) { MatchDrawScreen(state = ClubSamples.matchDetail(MatchStatus.SCHEDULED), onBackClick = {}, onViewTeams = {}) }

@Preview(name = "15 Sorteo completado · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun DrawNoche() = ScreenPreview(darkTheme = true) { MatchDrawScreen(state = ClubSamples.matchDetail(MatchStatus.SCHEDULED), onBackClick = {}, onViewTeams = {}) }

@Preview(name = "16 Resultado · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ResultTiza() = ScreenPreview(darkTheme = false) { MatchResultScreen(state = ClubSamples.matchDetail(MatchStatus.COMPLETED), onBackClick = {}) }

@Preview(name = "16 Resultado · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ResultNoche() = ScreenPreview(darkTheme = true) { MatchResultScreen(state = ClubSamples.matchDetail(MatchStatus.COMPLETED), onBackClick = {}) }
