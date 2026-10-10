package com.kikepb.club.presentation.match

import squadfy_app.feature.club.presentation.generated.resources.result_view
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.presentation.mapper.toTimelineKind
import com.kikepb.club.presentation.mapper.toTri
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBarBackButton
import com.kikepb.core.designsystem.triangulacion.components.PosChip
import com.kikepb.core.designsystem.triangulacion.components.RatingDelta
import com.kikepb.core.designsystem.triangulacion.components.Scoreboard
import com.kikepb.core.designsystem.triangulacion.components.TeamBadge
import com.kikepb.core.designsystem.triangulacion.components.TeamBalance
import com.kikepb.core.designsystem.triangulacion.components.Timeline
import com.kikepb.core.designsystem.triangulacion.components.TimelineEvent
import com.kikepb.core.designsystem.triangulacion.components.TimelineEventIcon
import com.kikepb.core.designsystem.triangulacion.components.TriAvatar
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriButtonVariant
import com.kikepb.core.designsystem.triangulacion.components.TriCaps
import com.kikepb.core.designsystem.triangulacion.components.TriCard
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriIconView
import com.kikepb.core.designsystem.triangulacion.components.TriNotice
import com.kikepb.core.designsystem.triangulacion.components.TriNoticeTone
import com.kikepb.core.designsystem.triangulacion.components.TriRoundIconButton
import com.kikepb.core.designsystem.triangulacion.components.TriSectionTitle
import com.kikepb.core.designsystem.triangulacion.components.TriTag
import com.kikepb.core.designsystem.triangulacion.components.TriTagTone
import com.kikepb.core.designsystem.triangulacion.components.TriTextButton
import com.kikepb.core.designsystem.triangulacion.format.TriDates
import com.kikepb.core.designsystem.triangulacion.format.formatClubTime
import com.kikepb.core.designsystem.triangulacion.format.formatLongDayCaps
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.designsystem.triangulacion.theme.tabular
import squadfy_app.core.designsystem.generated.resources.Res as DesignRes
import squadfy_app.core.designsystem.generated.resources.tri_team_a
import squadfy_app.core.designsystem.generated.resources.tri_team_b
import squadfy_app.feature.club.presentation.generated.resources.match_status_to_close
import squadfy_app.feature.club.presentation.generated.resources.match_team_count
import squadfy_app.feature.club.presentation.generated.resources.match_timeline_delete_hint
import squadfy_app.feature.club.presentation.generated.resources.match_timeline_title
import com.kikepb.core.designsystem.components.loading.SquadfyLoadingIndicator
import com.kikepb.club.presentation.components.LoadErrorCard
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.MatchEventModel
import com.kikepb.club.domain.model.MatchEventType
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.Team
import com.kikepb.club.domain.model.TeamBalanceModel
import com.kikepb.club.domain.model.TeamStrengthModel
import com.kikepb.club.domain.policy.TeamsPending
import com.kikepb.club.presentation.components.HintText
import com.kikepb.club.presentation.components.SectionCard
import com.kikepb.club.presentation.components.SectionTitle
import com.kikepb.club.presentation.mapper.initialsOf
import com.kikepb.club.presentation.mapper.label
import com.kikepb.club.presentation.util.formatDateTime
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.components.dialogs.SquadfyDestructiveConfirmationDialog
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.common_cancel
import squadfy_app.feature.club.presentation.generated.resources.common_save
import squadfy_app.feature.club.presentation.generated.resources.match_cancel
import squadfy_app.feature.club.presentation.generated.resources.match_cancel_description
import squadfy_app.feature.club.presentation.generated.resources.match_cancel_title
import squadfy_app.feature.club.presentation.generated.resources.match_complete
import squadfy_app.feature.club.presentation.generated.resources.match_complete_description
import squadfy_app.feature.club.presentation.generated.resources.match_complete_title
import squadfy_app.feature.club.presentation.generated.resources.match_decrease
import squadfy_app.feature.club.presentation.generated.resources.match_event_add_title
import squadfy_app.feature.club.presentation.generated.resources.match_event_assist
import squadfy_app.feature.club.presentation.generated.resources.match_event_delete
import squadfy_app.feature.club.presentation.generated.resources.match_event_goal
import squadfy_app.feature.club.presentation.generated.resources.match_event_minute
import squadfy_app.feature.club.presentation.generated.resources.match_event_red
import squadfy_app.feature.club.presentation.generated.resources.match_event_yellow
import squadfy_app.feature.club.presentation.generated.resources.match_events_empty
import squadfy_app.feature.club.presentation.generated.resources.match_events_title
import squadfy_app.feature.club.presentation.generated.resources.match_increase
import squadfy_app.feature.club.presentation.generated.resources.match_keep
import squadfy_app.feature.club.presentation.generated.resources.match_manual_score
import squadfy_app.feature.club.presentation.generated.resources.match_manual_score_clear
import squadfy_app.feature.club.presentation.generated.resources.match_manual_score_hint
import squadfy_app.feature.club.presentation.generated.resources.match_minutes_hint
import squadfy_app.feature.club.presentation.generated.resources.match_minutes_title
import squadfy_app.feature.club.presentation.generated.resources.match_minutes_value
import squadfy_app.feature.club.presentation.generated.resources.match_reopen
import squadfy_app.feature.club.presentation.generated.resources.match_reopen_description
import squadfy_app.feature.club.presentation.generated.resources.match_reopen_title
import squadfy_app.feature.club.presentation.generated.resources.match_report_mode
import squadfy_app.feature.club.presentation.generated.resources.match_report_mode_exit
import squadfy_app.feature.club.presentation.generated.resources.match_score
import squadfy_app.feature.club.presentation.generated.resources.match_score_manual
import squadfy_app.feature.club.presentation.generated.resources.match_undo
import squadfy_app.feature.club.presentation.generated.resources.former_member
import squadfy_app.feature.club.presentation.generated.resources.match_balance_expected
import squadfy_app.feature.club.presentation.generated.resources.match_balance_guest
import squadfy_app.feature.club.presentation.generated.resources.match_balance_team
import squadfy_app.feature.club.presentation.generated.resources.match_balance_title
import squadfy_app.feature.club.presentation.generated.resources.match_duration
import squadfy_app.feature.club.presentation.generated.resources.match_guest
import squadfy_app.feature.club.presentation.generated.resources.match_manual_edit
import squadfy_app.feature.club.presentation.generated.resources.match_manual_hint
import squadfy_app.feature.club.presentation.generated.resources.match_manual_invalid
import squadfy_app.feature.club.presentation.generated.resources.match_manual_positions
import squadfy_app.feature.club.presentation.generated.resources.match_my_team_a
import squadfy_app.feature.club.presentation.generated.resources.match_my_team_b
import squadfy_app.feature.club.presentation.generated.resources.match_no_position
import squadfy_app.feature.club.presentation.generated.resources.match_offline
import squadfy_app.feature.club.presentation.generated.resources.match_redraw
import squadfy_app.feature.club.presentation.generated.resources.match_redraw_description
import squadfy_app.feature.club.presentation.generated.resources.match_redraw_title
import squadfy_app.feature.club.presentation.generated.resources.match_save_teams
import squadfy_app.feature.club.presentation.generated.resources.match_team_a
import squadfy_app.feature.club.presentation.generated.resources.match_team_b
import squadfy_app.feature.club.presentation.generated.resources.match_teams_at_close
import squadfy_app.feature.club.presentation.generated.resources.match_teams_at_draw
import squadfy_app.feature.club.presentation.generated.resources.match_teams_pending
import squadfy_app.feature.club.presentation.generated.resources.match_title
import kotlin.math.roundToInt

@Composable
fun MatchDetailRoot(
    onBackClick: () -> Unit,
    onOpenResult: ((clubId: String, matchId: String) -> Unit)? = null,
    viewModel: MatchDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is MatchDetailEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
            is MatchDetailEvent.ShowUndo -> scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = event.message.asStringAsync(),
                    actionLabel = getString(Res.string.match_undo),
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.onAction(MatchDetailAction.OnUndoDeleteEvent(event.event))
            }
            MatchDetailEvent.Close -> onBackClick()
        }
    }
    // AC-009-04: while visible, pushes of this club are shown here instead of as notifications
    LifecycleStartEffect(Unit) {
        viewModel.onAction(MatchDetailAction.OnVisibilityChanged(true))
        onStopOrDispose { viewModel.onAction(MatchDetailAction.OnVisibilityChanged(false)) }
    }
    // APP-RN-08: refresh when coming back to the foreground (e.g. from a push)
    LifecycleResumeEffect(Unit) {
        viewModel.onAction(MatchDetailAction.OnRefresh)
        onPauseOrDispose { }
    }

    MatchDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState,
        onOpenResult = onOpenResult?.let { open -> { state.match?.let { open(it.clubId, it.id) } } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    state: MatchDetailState,
    onAction: (MatchDetailAction) -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onOpenResult: (() -> Unit)? = null
) {
    Scaffold(
        containerColor = Triangulacion.colors.surface100,
        topBar = {
            SquadfyTopBar(
                title = {
                    Text(text = stringResource(Res.string.match_title), style = Triangulacion.type.displayMd, color = Triangulacion.colors.ink, maxLines = 1)
                },
                navigationIcon = { SquadfyTopBarBackButton(onClick = onBackClick) },
                actions = { state.match?.let { StatusTag(it, state) } }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(MatchDetailAction.OnRefresh) },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Space.s4),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isStale) item(key = "stale") {
                    TriNotice(title = stringResource(Res.string.match_offline), tone = TriNoticeTone.Warn, icon = TriIcon.Alert)
                }
                if (state.match == null) {
                    if (state.loadFailed) item(key = "error") { LoadErrorCard(onRetry = { onAction(MatchDetailAction.OnRefresh) }) }
                    else item(key = "loading") { SquadfyLoadingIndicator() }
                    return@LazyColumn
                }
                item(key = "header") { MatchHeader(state = state) }
                if (onOpenResult != null && state.match.status == MatchStatus.COMPLETED) {
                    item(key = "result-link") {
                        TriButton(
                            text = stringResource(Res.string.result_view),
                            onClick = onOpenResult,
                            variant = TriButtonVariant.Secondary,
                            icon = TriIcon.Chart,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (state.match.hasTeams || state.match.status == MatchStatus.COMPLETED) {
                    item(key = "score") { ScoreCard(state = state, onAction = onAction) }
                }
                if (state.canRecord) {
                    item(key = "report-toggle") {
                        TriButton(
                            text = stringResource(if (state.reportMode) Res.string.match_report_mode_exit else Res.string.match_report_mode),
                            onClick = { onAction(MatchDetailAction.OnToggleReport) },
                            variant = TriButtonVariant.Secondary,
                            icon = if (state.reportMode) TriIcon.Check else TriIcon.Whistle,
                            enabled = state.editing == null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (state.match.events.isNotEmpty() || state.reportMode) item(key = "events") { EventsCard(state = state, onAction = onAction) }
                matchTeamsSection(state = state, onAction = onAction)
                state.visibleBalance?.let { balance ->
                    item(key = "balance") {
                        TriCard(modifier = Modifier.fillMaxWidth(), contentPadding = Space.s5) {
                            TeamBalance(ratingA = balance.teamA.averageRating, ratingB = balance.teamB.averageRating, expectedA = balance.teamAExpectedScore.toFloat())
                        }
                    }
                }
                if (state.canComplete || state.canReopen || state.canCancel) item(key = "cycle") { CycleActions(state = state, onAction = onAction) }
            }
        }
    }

    MatchDetailDialogs(state = state, onAction = onAction)
}

@Composable
private fun StatusTag(match: MatchModel, state: MatchDetailState) {
    when {
        match.status == MatchStatus.COMPLETED -> TriTag(text = stringResource(match.status.label))
        match.status == MatchStatus.CANCELLED -> TriTag(text = stringResource(match.status.label), tone = TriTagTone.Danger)
        state.canComplete -> TriTag(text = stringResource(Res.string.match_status_to_close), tone = TriTagTone.Warn)
        else -> TriTag(text = stringResource(match.status.label), tone = TriTagTone.Neutral)
    }
}

@Composable
private fun MatchHeader(state: MatchDetailState) {
    val match = state.match ?: return
    val local = TriDates.local(match.scheduledAt, state.timeZoneId)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TriCaps(text = "${formatLongDayCaps(local.date)} · ${formatClubTime(match.scheduledAt, state.timeZoneId)}", color = Triangulacion.colors.ink)
        val details = listOfNotNull(
            state.format?.let { stringResource(it.label) },
            stringResource(Res.string.match_duration, match.durationMinutes)
        ).joinToString(" · ")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HintText(text = details)
            state.myTeam?.let { team ->
                TeamBadge(team.toTri(), size = 18.dp)
                HintText(text = stringResource(if (team == Team.A) Res.string.match_my_team_a else Res.string.match_my_team_b))
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.matchTeamsSection(state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    val pending = state.pending
    if (pending != null) {
        item(key = "pending") {
            TriNotice(
                title = when (pending) {
                    is TeamsPending.AtClose -> stringResource(Res.string.match_teams_at_close, formatDateTime(pending.closesAt, state.timeZoneId))
                    is TeamsPending.AtDraw -> stringResource(Res.string.match_teams_at_draw, formatDateTime(pending.drawAt, state.timeZoneId))
                    TeamsPending.Pending -> stringResource(Res.string.match_teams_pending)
                },
                icon = TriIcon.Shuffle
            )
        }
    } else {
        item(key = "team-a") { TeamCard(team = Team.A, players = state.teamA, state = state, onAction = onAction) }
        item(key = "team-b") { TeamCard(team = Team.B, players = state.teamB, state = state, onAction = onAction) }
    }
    if (state.canRectify) item(key = "rectify") { RectifyActions(state = state, onAction = onAction) }
}

@Composable
private fun TeamCard(team: Team, players: List<TeamPlayer>, state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    val isMine = state.myTeam == team
    val colors = Triangulacion.colors
    TriCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TeamBadge(team.toTri(), size = 22.dp)
            Text(
                text = stringResource(Res.string.match_team_count, stringResource(if (team == Team.A) Res.string.match_team_a else Res.string.match_team_b), players.size),
                style = Triangulacion.type.title,
                color = if (team == Team.A) colors.teamAText else colors.teamBText
            )
        }
        if (state.editing != null) PositionsSummary(players = players)
        Spacer(Modifier.height(6.dp))
        // Grouped by position (AC-006-01): goalkeepers first, players without position last
        players.sortedBy { it.position?.ordinal ?: Int.MAX_VALUE }.forEach { player ->
            PlayerRow(
                player = player,
                team = team,
                highlighted = isMine && player.isMe,
                editing = state.editing != null,
                onClick = { onAction(MatchDetailAction.OnMovePlayer(player.id)) },
                state = state,
                ratingChange = state.match?.ratingChanges?.get(player.id)?.takeIf { !player.isGuest }
            )
            // Guests have no stats (BE-008 RN-A6): only members get report controls
            if (state.reportMode && !player.isGuest) ReportControls(player = player, state = state, onAction = onAction)
        }
    }
}

@Composable
private fun PositionsSummary(players: List<TeamPlayer>) {
    // While editing the balance is not recomputed locally: only the positions are compared (AC-006-05)
    val counts = players.groupingBy { it.position }.eachCount().entries
        .sortedBy { it.key?.ordinal ?: Int.MAX_VALUE }
        .map { (position, count) -> stringResource(Res.string.match_manual_positions, position?.let { stringResource(it.label) } ?: stringResource(Res.string.match_no_position), count.toString()) }
    HintText(text = counts.joinToString(" · "))
}

@Composable
private fun PlayerRow(
    player: TeamPlayer,
    team: Team,
    highlighted: Boolean,
    editing: Boolean,
    onClick: () -> Unit,
    state: MatchDetailState,
    ratingChange: Int? = null
) {
    val colors = Triangulacion.colors
    val name = player.name ?: stringResource(Res.string.former_member)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .background(if (highlighted) colors.successSoft else Color.Transparent)
            .then(if (editing) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TriAvatar(name = name, imageUrl = player.profilePictureUrl, size = 36.dp, team = team.toTri().takeIf { player.profilePictureUrl == null }, isMe = highlighted)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = Triangulacion.type.label.copy(fontSize = Triangulacion.type.body.fontSize), color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val details = listOfNotNull(
                player.shirtNumber?.let { "#$it" },
                stringResource(Res.string.match_guest).takeIf { player.isGuest }
            ).joinToString(" · ")
            if (details.isNotBlank()) HintText(text = details)
        }
        player.position.toTri()?.let { PosChip(it) }
        EventCounts(player = player, state = state)
        // AC-008-08: match rating, with its glyph and number
        ratingChange?.let { RatingDelta(it) }
        if (editing) TriIconView(icon = TriIcon.Swap, contentDescription = null, size = 20.dp, tint = colors.brand)
    }
}

/** Goals, assists and cards of a player as glyphs with counts; minutes in report mode or once completed. */
@Composable
private fun EventCounts(player: TeamPlayer, state: MatchDetailState) {
    val match = state.match ?: return
    if (player.isGuest) return
    val counts = match.events.filter { it.clubMemberId == player.id }.groupingBy { it.type }.eachCount()
    val showMinutes = state.reportMode || match.status == MatchStatus.COMPLETED
    if (counts.isEmpty() && !showMinutes) return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        counts.entries.sortedBy { it.key.ordinal }.forEach { (type, count) ->
            TimelineEventIcon(kind = type.toTimelineKind(), modifier = Modifier.size(24.dp))
            if (count > 1) Text(text = count.toString(), style = Triangulacion.type.label.tabular, color = Triangulacion.colors.ink)
        }
        if (showMinutes) Text(text = stringResource(Res.string.match_minutes_value, match.minutesOf(player.id)), style = Triangulacion.type.bodySm.tabular, color = Triangulacion.colors.inkMuted)
    }
}

@Composable
private fun RectifyActions(state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    val editing = state.editing
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (editing == null) {
            TriButton(
                text = stringResource(Res.string.match_redraw),
                onClick = { onAction(MatchDetailAction.OnRedrawClick) },
                icon = TriIcon.Shuffle,
                isLoading = state.isWorking,
                modifier = Modifier.fillMaxWidth()
            )
            TriButton(
                text = stringResource(Res.string.match_manual_edit),
                onClick = { onAction(MatchDetailAction.OnStartManualEdit) },
                variant = TriButtonVariant.Secondary,
                icon = TriIcon.Edit,
                enabled = !state.isWorking,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            TriNotice(
                title = stringResource(if (editing.isValid) Res.string.match_manual_hint else Res.string.match_manual_invalid),
                tone = if (editing.isValid) TriNoticeTone.Info else TriNoticeTone.Warn
            )
            TriButton(
                text = stringResource(Res.string.match_save_teams),
                onClick = { onAction(MatchDetailAction.OnSaveManualEdit) },
                enabled = editing.isValid,
                isLoading = state.isWorking,
                modifier = Modifier.fillMaxWidth()
            )
            TriButton(
                text = stringResource(Res.string.common_cancel),
                onClick = { onAction(MatchDetailAction.OnCancelManualEdit) },
                variant = TriButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

val MatchEventType.label
    get() = when (this) {
        MatchEventType.GOAL -> Res.string.match_event_goal
        MatchEventType.ASSIST -> Res.string.match_event_assist
        MatchEventType.YELLOW_CARD -> Res.string.match_event_yellow
        MatchEventType.RED_CARD -> Res.string.match_event_red
    }

/** Report mode (AC-007-01): one chip per event type with its glyph, plus the minutes. */
@Composable
private fun ReportControls(player: TeamPlayer, state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    val match = state.match ?: return
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 46.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MatchEventType.entries.forEach { type ->
            ReportChip(text = stringResource(type.label), enabled = !state.isWorking, onClick = { onAction(MatchDetailAction.OnAddEventClick(player.id, type)) }) {
                TimelineEventIcon(kind = type.toTimelineKind(), modifier = Modifier.size(22.dp))
            }
        }
        ReportChip(
            text = stringResource(Res.string.match_minutes_value, match.minutesOf(player.id)),
            enabled = !state.isWorking,
            onClick = { onAction(MatchDetailAction.OnEditMinutesClick(player.id)) }
        ) { TriIconView(icon = TriIcon.Clock, contentDescription = null, size = 18.dp) }
    }
}

@Composable
private fun ReportChip(text: String, enabled: Boolean, onClick: () -> Unit, icon: @Composable () -> Unit) {
    val colors = Triangulacion.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(Radius.sm))
            .border(1.5.dp, colors.lineStrong, RoundedCornerShape(Radius.sm))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        icon()
        Text(text = text, style = Triangulacion.type.label, color = if (enabled) colors.ink else colors.inkSubtle, maxLines = 1)
    }
}

@Composable
private fun ScoreCard(state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    val match = state.match ?: return
    TriCard(modifier = Modifier.fillMaxWidth(), contentPadding = Space.s5) {
        // Official score: from goal events, or the manual one (APP-RN-10, BE-008 RN-E2)
        Scoreboard(
            scoreA = match.teamAScore,
            scoreB = match.teamBScore,
            nameA = stringResource(DesignRes.string.tri_team_a),
            nameB = stringResource(DesignRes.string.tri_team_b),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (match.isManualScore) TriTag(text = stringResource(Res.string.match_score_manual), tone = TriTagTone.Dashed)
        }
        if (state.canSetManualScore) {
            Spacer(Modifier.height(Space.s3))
            HintText(text = stringResource(Res.string.match_manual_score_hint))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TriButton(
                    text = stringResource(Res.string.match_manual_score),
                    onClick = { onAction(MatchDetailAction.OnManualScoreClick) },
                    variant = TriButtonVariant.Secondary,
                    enabled = !state.isWorking
                )
                if (match.isManualScore) {
                    TriTextButton(text = stringResource(Res.string.match_manual_score_clear), onClick = { onAction(MatchDetailAction.OnClearManualScore) }, enabled = !state.isWorking)
                }
            }
        }
    }
}

/** Cronología (spec 018): goals, assists and cards on both sides of the minute axis. */
@Composable
private fun EventsCard(state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    val match = state.match ?: return
    val formerMember = stringResource(Res.string.former_member)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TriSectionTitle(text = stringResource(Res.string.match_timeline_title))
        if (match.events.isEmpty()) {
            HintText(text = stringResource(Res.string.match_events_empty))
        } else {
            if (state.canRecord) HintText(text = stringResource(Res.string.match_timeline_delete_hint))
            val events = match.events.sortedWith(compareBy({ it.minute ?: Int.MAX_VALUE }, { it.createdAt }))
            Timeline(
                events = events.map { event ->
                    TimelineEvent(
                        id = event.id,
                        minute = event.minute,
                        team = match.teamOf(event.clubMemberId)?.toTri(),
                        kind = event.type.toTimelineKind(),
                        who = state.player(event.clubMemberId, isGuest = false).name ?: formerMember
                    )
                },
                onEventClick = if (state.canRecord && !state.isWorking) {
                    { timelineEvent -> events.firstOrNull { it.id == timelineEvent.id }?.let { onAction(MatchDetailAction.OnDeleteEvent(it)) } }
                } else null
            )
        }
    }
}

@Composable
private fun CycleActions(state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = Space.s2)) {
        if (state.canComplete) {
            TriButton(
                text = stringResource(Res.string.match_complete),
                onClick = { onAction(MatchDetailAction.OnCompleteClick) },
                icon = TriIcon.Check,
                isLoading = state.isWorking,
                large = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (state.canReopen) {
            TriButton(
                text = stringResource(Res.string.match_reopen),
                onClick = { onAction(MatchDetailAction.OnReopenClick) },
                variant = TriButtonVariant.Secondary,
                isLoading = state.isWorking,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (state.canCancel) {
            TriButton(
                text = stringResource(Res.string.match_cancel),
                onClick = { onAction(MatchDetailAction.OnCancelMatchClick) },
                variant = TriButtonVariant.Danger,
                enabled = !state.isWorking,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MatchDetailDialogs(state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    val dismiss = { onAction(MatchDetailAction.OnDismissDialog) }
    val match = state.match
    when (val dialog = state.dialog) {
        MatchDetailDialog.ConfirmRedraw -> SquadfyDestructiveConfirmationDialog(
            title = stringResource(Res.string.match_redraw_title),
            description = stringResource(Res.string.match_redraw_description),
            confirmButtonText = stringResource(Res.string.match_redraw),
            cancelButtonText = stringResource(Res.string.common_cancel),
            onConfirmClick = { onAction(MatchDetailAction.OnConfirmRedraw) },
            onCancelClick = dismiss,
            onDismiss = dismiss
        )
        MatchDetailDialog.ConfirmComplete -> ConfirmDialog(
            title = stringResource(Res.string.match_complete_title),
            text = stringResource(Res.string.match_complete_description, match?.teamAScore ?: 0, match?.teamBScore ?: 0),
            confirm = stringResource(Res.string.match_complete),
            onConfirm = { onAction(MatchDetailAction.OnConfirmComplete) },
            onDismiss = dismiss
        )
        MatchDetailDialog.ConfirmReopen -> ConfirmDialog(
            title = stringResource(Res.string.match_reopen_title),
            text = stringResource(Res.string.match_reopen_description),
            confirm = stringResource(Res.string.match_reopen),
            onConfirm = { onAction(MatchDetailAction.OnConfirmReopen) },
            onDismiss = dismiss
        )
        MatchDetailDialog.ConfirmCancel -> SquadfyDestructiveConfirmationDialog(
            title = stringResource(Res.string.match_cancel_title),
            description = stringResource(Res.string.match_cancel_description),
            confirmButtonText = stringResource(Res.string.match_cancel),
            cancelButtonText = stringResource(Res.string.match_keep),
            onConfirmClick = { onAction(MatchDetailAction.OnConfirmCancelMatch) },
            onCancelClick = dismiss,
            onDismiss = dismiss
        )
        is MatchDetailDialog.AddEvent -> {
            val name = state.player(dialog.playerId, isGuest = false).name ?: stringResource(Res.string.former_member)
            AlertDialog(
                onDismissRequest = dismiss,
                title = { Text(text = stringResource(Res.string.match_event_add_title, stringResource(dialog.type.label))) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TimelineEventIcon(kind = dialog.type.toTimelineKind())
                            Text(text = name, style = Triangulacion.type.bodyLg, color = Triangulacion.colors.ink)
                        }
                        SquadfyTextField(state = dialog.minute, title = stringResource(Res.string.match_event_minute), singleLine = true)
                    }
                },
                confirmButton = { TextButton(onClick = { onAction(MatchDetailAction.OnConfirmAddEvent) }) { Text(text = stringResource(Res.string.common_save)) } },
                dismissButton = { TextButton(onClick = dismiss) { Text(text = stringResource(Res.string.common_cancel)) } }
            )
        }
        is MatchDetailDialog.EditMinutes -> AlertDialog(
            onDismissRequest = dismiss,
            title = { Text(text = stringResource(Res.string.match_minutes_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = state.player(dialog.playerId, isGuest = false).name ?: stringResource(Res.string.former_member))
                    SquadfyTextField(state = dialog.minutes, title = stringResource(Res.string.match_minutes_title), singleLine = true)
                    Text(
                        text = stringResource(Res.string.match_minutes_hint, match?.durationMinutes ?: 0),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (dialog.isInvalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.extended.textPlaceholder
                    )
                }
            },
            confirmButton = { TextButton(onClick = { onAction(MatchDetailAction.OnConfirmMinutes) }) { Text(text = stringResource(Res.string.common_save)) } },
            dismissButton = { TextButton(onClick = dismiss) { Text(text = stringResource(Res.string.common_cancel)) } }
        )
        is MatchDetailDialog.ManualScore -> AlertDialog(
            onDismissRequest = dismiss,
            title = { Text(text = stringResource(Res.string.match_manual_score)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ScoreStepper(label = stringResource(Res.string.match_team_a), value = dialog.teamA) { delta ->
                        onAction(MatchDetailAction.OnManualScoreChanged(Team.A, delta))
                    }
                    ScoreStepper(label = stringResource(Res.string.match_team_b), value = dialog.teamB) { delta ->
                        onAction(MatchDetailAction.OnManualScoreChanged(Team.B, delta))
                    }
                    HintText(text = stringResource(Res.string.match_manual_score_hint))
                }
            },
            confirmButton = { TextButton(onClick = { onAction(MatchDetailAction.OnConfirmManualScore) }) { Text(text = stringResource(Res.string.common_save)) } },
            dismissButton = { TextButton(onClick = dismiss) { Text(text = stringResource(Res.string.common_cancel)) } }
        )
        null -> Unit
    }
}

@Composable
private fun ScoreStepper(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, modifier = Modifier.weight(1f))
        TriRoundIconButton(icon = TriIcon.Minus, contentDescription = stringResource(Res.string.match_decrease), onClick = { onChange(-1) }, filled = true)
        Text(text = value.toString(), style = Triangulacion.type.displayMd.tabular, color = Triangulacion.colors.ink, modifier = Modifier.padding(horizontal = 12.dp))
        TriRoundIconButton(icon = TriIcon.Plus, contentDescription = stringResource(Res.string.match_increase), onClick = { onChange(1) }, filled = true)
    }
}

@Composable
private fun ConfirmDialog(title: String, text: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(text = confirm) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = stringResource(Res.string.common_cancel)) } }
    )
}
