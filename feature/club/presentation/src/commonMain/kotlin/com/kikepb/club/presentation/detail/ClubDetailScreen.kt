@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.kikepb.club.presentation.detail

import com.kikepb.club.presentation.settings.ClubSettingsContent
import com.kikepb.club.presentation.standings.StandingsContent
import com.kikepb.club.presentation.teams.TeamsView
import com.kikepb.club.presentation.teams.TeamsState
import com.kikepb.club.presentation.teams.TeamsContent
import com.kikepb.club.presentation.announcement.AnnouncementState
import com.kikepb.club.presentation.announcement.AnnouncementContent
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.presentation.preview.ClubSamples
import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.backhandler.BackHandler
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.presentation.mapper.label
import com.kikepb.club.presentation.teams.TeamsTab
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBarBackButton
import com.kikepb.core.designsystem.triangulacion.components.ClubCrest
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriRoundIconButton
import com.kikepb.core.designsystem.triangulacion.components.TriTabs
import com.kikepb.core.designsystem.triangulacion.components.TriTag
import com.kikepb.core.designsystem.triangulacion.theme.CrestTone
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import squadfy_app.feature.club.presentation.generated.resources.club_members_count
import squadfy_app.feature.club.presentation.generated.resources.club_settings_title
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.presentation.detail.components.SquadfyClubDetailBanner
import com.kikepb.club.presentation.detail.components.SquadfyClubDetailMembersTab
import com.kikepb.club.presentation.components.NotificationPermissionPrompt
import com.kikepb.club.presentation.settings.ClubSettingsTab
import com.kikepb.club.presentation.standings.StandingsTab
import org.jetbrains.compose.resources.stringResource
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.club_detail_load_error
import squadfy_app.feature.club.presentation.generated.resources.club_detail_load_error_hint
import squadfy_app.feature.club.presentation.generated.resources.club_detail_loading
import squadfy_app.feature.club.presentation.generated.resources.club_detail_title
import squadfy_app.feature.club.presentation.generated.resources.common_retry
import com.kikepb.club.presentation.detail.components.SquadfyClubDetailTabRow
import com.kikepb.club.presentation.announcement.AnnouncementTab
import com.kikepb.club.presentation.detail.model.ClubDetailTabModel
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ClubDetailRoot(
    onBackClick: () -> Unit,
    onMemberClick: (clubId: String, memberId: String) -> Unit,
    onOpenBans: (clubId: String) -> Unit,
    onOpenSchedule: (clubId: String) -> Unit,
    onOpenMatch: (clubId: String, matchId: String) -> Unit,
    onOpenAbsences: (clubId: String) -> Unit,
    onLeftClub: () -> Unit,
    viewModel: ClubDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is ClubDetailEvent.ShowMessage ->
                scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
        }
    }

    // Members who joined meanwhile (or a renamed club) show up when coming back to the club
    LifecycleResumeEffect(Unit) {
        viewModel.onAction(ClubDetailAction.OnResume)
        onPauseOrDispose { }
    }

    NotificationPermissionPrompt()

    ClubDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        onMemberClick = onMemberClick,
        onOpenBans = onOpenBans,
        onOpenSchedule = onOpenSchedule,
        onOpenMatch = onOpenMatch,
        onOpenAbsences = onOpenAbsences,
        onLeftClub = onLeftClub,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubDetailScreen(
    state: ClubDetailState,
    onAction: (ClubDetailAction) -> Unit,
    onBackClick: () -> Unit,
    onMemberClick: (clubId: String, memberId: String) -> Unit,
    onOpenBans: (clubId: String) -> Unit,
    onOpenSchedule: (clubId: String) -> Unit,
    onOpenMatch: (clubId: String, matchId: String) -> Unit,
    onOpenAbsences: (clubId: String) -> Unit = {},
    onLeftClub: () -> Unit,
    snackbarHostState: SnackbarHostState,
    /** Previews draw the tabs themselves (the real ones use their own ViewModels). */
    tabContent: (@Composable (ClubDetailTabModel) -> Unit)? = null,
    initialTab: ClubDetailTabModel = ClubDetailTabModel.MATCH
) {
    val club = state.club
    var selectedTabIndex by rememberSaveable { mutableStateOf(initialTab.ordinal) }

    val inSettings = ClubDetailTabModel.entries[selectedTabIndex] == ClubDetailTabModel.SETTINGS
    // Settings open from the header button; back returns to the match tab
    BackHandler(enabled = inSettings) { selectedTabIndex = ClubDetailTabModel.MATCH.ordinal }
    Scaffold(
        containerColor = Triangulacion.colors.surface100,
        topBar = {
            if (club == null) {
                SquadfyTopBar(title = stringResource(Res.string.club_detail_title), onBackClick = onBackClick)
            } else {
                ClubHeader(
                    club = club,
                    role = state.myMembership?.role,
                    onBackClick = if (inSettings) ({ selectedTabIndex = ClubDetailTabModel.MATCH.ordinal }) else onBackClick,
                    onSettingsClick = { selectedTabIndex = ClubDetailTabModel.SETTINGS.ordinal },
                    settingsSelected = inSettings
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading && club == null -> LoadingContent(Modifier.padding(padding))
            club == null -> {
                EmptyContent(
                    modifier = Modifier.padding(paddingValues = padding),
                    onRetry = { onAction(ClubDetailAction.OnRefresh) }
                )
            }
            else -> ClubContent(
                club = club,
                state = state,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                onMemberClick = { memberId -> onMemberClick(club.id, memberId) },
                onOpenBans = { onOpenBans(club.id) },
                onOpenSchedule = { onOpenSchedule(club.id) },
                onOpenMatch = { matchId -> onOpenMatch(club.id, matchId) },
                onOpenAbsences = { onOpenAbsences(club.id) },
                onLeftClub = onLeftClub,
                snackbarHostState = snackbarHostState,
                tabContent = tabContent,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ClubContent(
    club: ClubModel,
    state: ClubDetailState,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onMemberClick: (String) -> Unit,
    onOpenBans: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenMatch: (matchId: String) -> Unit,
    onOpenAbsences: () -> Unit,
    onLeftClub: () -> Unit,
    snackbarHostState: SnackbarHostState,
    tabContent: (@Composable (ClubDetailTabModel) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        val visibleIndex = ClubDetailTabModel.tabs.indexOf(ClubDetailTabModel.entries[selectedTabIndex])
        TriTabs(
            items = ClubDetailTabModel.tabs.map { stringResource(it.title) },
            selectedIndex = visibleIndex,
            onSelect = { onTabSelected(ClubDetailTabModel.tabs[it].ordinal) },
            modifier = Modifier.padding(horizontal = Space.s4, vertical = Space.s2)
        )
        AnimatedContent(
            targetState = selectedTabIndex,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
            modifier = Modifier.weight(1f)
        ) { tabIndex ->
            if (tabContent != null) {
                tabContent(ClubDetailTabModel.entries[tabIndex])
                return@AnimatedContent
            }
            when (ClubDetailTabModel.entries[tabIndex]) {
                ClubDetailTabModel.MATCH -> AnnouncementTab(
                    snackbarHostState = snackbarHostState,
                    onOpenSchedule = onOpenSchedule,
                    onNotMemberAnymore = onLeftClub,
                    onOpenMatch = onOpenMatch
                )
                ClubDetailTabModel.TEAMS -> TeamsTab(onOpenMatch = onOpenMatch)
                ClubDetailTabModel.CLASSIFICATION -> StandingsTab(club = club, onMemberClick = onMemberClick)
                ClubDetailTabModel.MEMBERS -> SquadfyClubDetailMembersTab(members = state.members, onMemberClick = onMemberClick)
                ClubDetailTabModel.SETTINGS -> ClubSettingsTab(
                    club = club,
                    myMembership = state.myMembership,
                    snackbarHostState = snackbarHostState,
                    onOpenBans = onOpenBans,
                    onOpenSchedule = onOpenSchedule,
                    onOpenAbsences = onOpenAbsences,
                    onLeftClub = onLeftClub
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Text(
                text = stringResource(Res.string.club_detail_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textPlaceholder
            )
        }
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Text(
                text = stringResource(Res.string.club_detail_load_error),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.extended.textSecondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(Res.string.club_detail_load_error_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.extended.textPlaceholder,
                textAlign = TextAlign.Center
            )
            SquadfyButton(text = stringResource(Res.string.common_retry), onClick = onRetry)
        }
    }
}

/** Club header (spec 018): back, crest, name, my role and the member count, and the settings button. */
@Composable
private fun ClubHeader(
    club: ClubModel,
    role: ClubMemberRole?,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    settingsSelected: Boolean
) {
    val colors = Triangulacion.colors
    SquadfyTopBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ClubCrest(name = club.name, size = 44.dp, tone = CrestTone.forKey(club.id), logoUrl = club.clubLogoUrl)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = club.name, style = Triangulacion.type.displayMd, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        role?.let { TriTag(text = stringResource(it.label)) }
                        Text(text = stringResource(Res.string.club_members_count, club.membersCount), style = Triangulacion.type.bodySm, color = colors.inkMuted)
                    }
                }
            }
        },
        navigationIcon = { SquadfyTopBarBackButton(onClick = onBackClick) },
        actions = {
            TriRoundIconButton(
                icon = if (settingsSelected) TriIcon.X else TriIcon.Sliders,
                contentDescription = stringResource(Res.string.club_settings_title),
                onClick = onSettingsClick,
                filled = settingsSelected
            )
        }
    )
}

/** Preview helper: the club screen with [tab] drawn from sample state. */
@Composable
private fun ClubDetailWithTab(
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

// Previews (spec 018): Tiza and Noche with the data of the mockups

@Preview(name = "04 Club · Partido · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchTizaPreview() = ScreenPreview { ClubDetailWithTab(ClubDetailTabModel.MATCH) }

@Preview(name = "04 Club · Partido · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchNochePreview() = ScreenPreview(darkTheme = true) { ClubDetailWithTab(ClubDetailTabModel.MATCH) }

@Preview(name = "04b Club · Partido convocado · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchConfirmedTizaPreview() = ScreenPreview { ClubDetailWithTab(ClubDetailTabModel.MATCH, ClubSamples.announcement(MyEnrollmentStatus.CONFIRMED)) }

@Preview(name = "04b Club · Partido convocado · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchConfirmedNochePreview() = ScreenPreview(darkTheme = true) { ClubDetailWithTab(ClubDetailTabModel.MATCH, ClubSamples.announcement(MyEnrollmentStatus.CONFIRMED)) }

@Preview(name = "04c Club · Partido en espera · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchWaitlistTizaPreview() = ScreenPreview { ClubDetailWithTab(ClubDetailTabModel.MATCH, ClubSamples.announcement(MyEnrollmentStatus.WAITLISTED)) }

@Preview(name = "04c Club · Partido en espera · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchWaitlistNochePreview() = ScreenPreview(darkTheme = true) { ClubDetailWithTab(ClubDetailTabModel.MATCH, ClubSamples.announcement(MyEnrollmentStatus.WAITLISTED)) }

@Preview(name = "04d Club · Partido cerrada · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchClosedTizaPreview() = ScreenPreview { ClubDetailWithTab(ClubDetailTabModel.MATCH, ClubSamples.announcementClosed) }

@Preview(name = "04d Club · Partido cerrada · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMatchClosedNochePreview() = ScreenPreview(darkTheme = true) { ClubDetailWithTab(ClubDetailTabModel.MATCH, ClubSamples.announcementClosed) }

@Preview(name = "05 Club · Equipos en el campo · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubTeamsPitchTizaPreview() = ScreenPreview { ClubDetailWithTab(ClubDetailTabModel.TEAMS) }

@Preview(name = "05 Club · Equipos en el campo · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubTeamsPitchNochePreview() = ScreenPreview(darkTheme = true) { ClubDetailWithTab(ClubDetailTabModel.TEAMS) }

@Preview(name = "05b Club · Equipos en lista · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubTeamsListTizaPreview() = ScreenPreview { ClubDetailWithTab(ClubDetailTabModel.TEAMS, teams = ClubSamples.teams.copy(view = TeamsView.LIST)) }

@Preview(name = "05b Club · Equipos en lista · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubTeamsListNochePreview() = ScreenPreview(darkTheme = true) { ClubDetailWithTab(ClubDetailTabModel.TEAMS, teams = ClubSamples.teams.copy(view = TeamsView.LIST)) }

@Preview(name = "08 Club · Tabla · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubTableTizaPreview() = ScreenPreview { ClubDetailWithTab(ClubDetailTabModel.CLASSIFICATION) }

@Preview(name = "08 Club · Tabla · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubTableNochePreview() = ScreenPreview(darkTheme = true) { ClubDetailWithTab(ClubDetailTabModel.CLASSIFICATION) }

@Preview(name = "08b Club · Miembros · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMembersTizaPreview() = ScreenPreview { ClubDetailWithTab(ClubDetailTabModel.MEMBERS) }

@Preview(name = "08b Club · Miembros · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubMembersNochePreview() = ScreenPreview(darkTheme = true) { ClubDetailWithTab(ClubDetailTabModel.MEMBERS) }

@Preview(name = "08c Club · Ajustes · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ClubSettingsTizaPreview() = ScreenPreview { ClubDetailWithTab(ClubDetailTabModel.SETTINGS) }

@Preview(name = "08c Club · Ajustes · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ClubSettingsNochePreview() = ScreenPreview(darkTheme = true) { ClubDetailWithTab(ClubDetailTabModel.SETTINGS) }
