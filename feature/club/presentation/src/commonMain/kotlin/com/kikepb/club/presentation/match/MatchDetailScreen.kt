package com.kikepb.club.presentation.match

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

    MatchDetailScreen(state = state, onAction = viewModel::onAction, onBackClick = onBackClick, snackbarHostState = snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    state: MatchDetailState,
    onAction: (MatchDetailAction) -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        topBar = { SquadfyTopBar(title = stringResource(Res.string.match_title), onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(MatchDetailAction.OnRefresh) },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isStale) item(key = "stale") { HintText(text = stringResource(Res.string.match_offline)) }
                state.match ?: return@LazyColumn
                item(key = "header") { MatchHeader(state = state) }
                if (state.match.hasTeams || state.match.status == MatchStatus.COMPLETED) {
                    item(key = "score") { ScoreCard(state = state, onAction = onAction) }
                }
                if (state.canRecord) {
                    item(key = "report-toggle") {
                        OutlinedButton(onClick = { onAction(MatchDetailAction.OnToggleReport) }, enabled = state.editing == null, modifier = Modifier.fillMaxWidth()) {
                            Text(text = stringResource(if (state.reportMode) Res.string.match_report_mode_exit else Res.string.match_report_mode))
                        }
                    }
                }
                matchTeamsSection(state = state, onAction = onAction)
                if (state.match.events.isNotEmpty() || state.reportMode) item(key = "events") { EventsCard(state = state, onAction = onAction) }
                state.visibleBalance?.let { balance ->
                    item(key = "balance") { BalancePanel(balance = balance, state = state) }
                }
                if (state.canComplete || state.canReopen || state.canCancel) item(key = "cycle") { CycleActions(state = state, onAction = onAction) }
            }
        }
    }

    MatchDetailDialogs(state = state, onAction = onAction)
}

@Composable
private fun MatchHeader(state: MatchDetailState) {
    val match = state.match ?: return
    SectionCard {
        Text(
            text = formatDateTime(match.scheduledAt, state.timeZoneId),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
        val details = listOfNotNull(
            state.format?.let { stringResource(it.label) },
            stringResource(Res.string.match_duration, match.durationMinutes)
        ).joinToString(" · ")
        HintText(text = details)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text(text = stringResource(match.status.label)) })
            when (state.myTeam) {
                Team.A -> AssistChip(onClick = {}, label = { Text(text = stringResource(Res.string.match_my_team_a)) })
                Team.B -> AssistChip(onClick = {}, label = { Text(text = stringResource(Res.string.match_my_team_b)) })
                null -> Unit
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.matchTeamsSection(state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    val pending = state.pending
    if (pending != null) {
        item(key = "pending") {
            SectionCard {
                Text(
                    text = when (pending) {
                        is TeamsPending.AtClose -> stringResource(Res.string.match_teams_at_close, formatDateTime(pending.closesAt, state.timeZoneId))
                        is TeamsPending.AtDraw -> stringResource(Res.string.match_teams_at_draw, formatDateTime(pending.drawAt, state.timeZoneId))
                        TeamsPending.Pending -> stringResource(Res.string.match_teams_pending)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.extended.textSecondary
                )
            }
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
    SectionCard {
        SectionTitle(text = stringResource(if (team == Team.A) Res.string.match_team_a else Res.string.match_team_b) + " · ${players.size}")
        if (state.editing != null) PositionsSummary(players = players)
        // Grouped by position (AC-006-01): goalkeepers first, players without position last
        players.sortedBy { it.position?.ordinal ?: Int.MAX_VALUE }.forEach { player ->
            PlayerRow(
                player = player,
                highlighted = isMine && player.isMe,
                editing = state.editing != null,
                onClick = { onAction(MatchDetailAction.OnMovePlayer(player.id)) },
                stats = playerStats(player = player, state = state),
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
private fun PlayerRow(player: TeamPlayer, highlighted: Boolean, editing: Boolean, onClick: () -> Unit, stats: String? = null, ratingChange: Int? = null) {
    val name = player.name ?: stringResource(Res.string.former_member)
    Surface(
        modifier = Modifier.fillMaxWidth().then(if (editing) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        color = if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SquadfyAvatarPhoto(displayText = initialsOf(name), imageUrl = player.profilePictureUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                val details = listOfNotNull(
                    player.shirtNumber?.let { "#$it" },
                    player.position?.let { stringResource(it.label) },
                    stringResource(Res.string.match_guest).takeIf { player.isGuest }
                ).joinToString(" · ")
                if (details.isNotBlank()) HintText(text = details)
            }
            stats?.let { Text(text = it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.extended.textSecondary) }
            // AC-008-08: match rating, green when it goes up and red when it goes down
            ratingChange?.let { change ->
                Text(
                    text = if (change > 0) "+$change" else "$change",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (change >= 0) MaterialTheme.colorScheme.extended.success else MaterialTheme.colorScheme.error
                )
            }
            if (editing) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.CompareArrows, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun RectifyActions(state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    val editing = state.editing
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (editing == null) {
            SquadfyButton(
                text = stringResource(Res.string.match_redraw),
                onClick = { onAction(MatchDetailAction.OnRedrawClick) },
                isLoading = state.isWorking,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(onClick = { onAction(MatchDetailAction.OnStartManualEdit) }, enabled = !state.isWorking, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(Res.string.match_manual_edit))
            }
        } else {
            HintText(text = stringResource(if (editing.isValid) Res.string.match_manual_hint else Res.string.match_manual_invalid))
            SquadfyButton(
                text = stringResource(Res.string.match_save_teams),
                onClick = { onAction(MatchDetailAction.OnSaveManualEdit) },
                enabled = editing.isValid,
                isLoading = state.isWorking,
                modifier = Modifier.fillMaxWidth()
            )
            SquadfyButton(
                text = stringResource(Res.string.common_cancel),
                onClick = { onAction(MatchDetailAction.OnCancelManualEdit) },
                style = SquadfyButtonStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BalancePanel(balance: TeamBalanceModel, state: MatchDetailState) {
    SectionCard {
        SectionTitle(text = stringResource(Res.string.match_balance_title))
        val percentA = (balance.teamAExpectedScore * 100).roundToInt().coerceIn(0, 100)
        Text(
            text = stringResource(Res.string.match_balance_expected, "$percentA %", "${100 - percentA} %"),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
        LinearProgressIndicator(progress = { percentA / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp))
        TeamStrength(name = stringResource(Res.string.match_team_a), strength = balance.teamA, state = state)
        TeamStrength(name = stringResource(Res.string.match_team_b), strength = balance.teamB, state = state)
    }
}

@Composable
private fun TeamStrength(name: String, strength: TeamStrengthModel, state: MatchDetailState) {
    Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(Res.string.match_balance_team, name, strength.averageRating, strength.totalRating),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        strength.playerRatings.forEach { rating ->
            val player = state.player(rating.id, rating.isGuest)
            val label = if (rating.isGuest) {
                "${player.name.orEmpty()} · " + stringResource(Res.string.match_balance_guest, rating.rating)
            } else {
                "${player.name ?: stringResource(Res.string.former_member)} · ${rating.rating}"
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(modifier = Modifier.size(6.dp), shape = RoundedCornerShape(3.dp), color = MaterialTheme.colorScheme.primary) {}
                HintText(text = label)
            }
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

private val MatchEventType.symbol: String
    get() = when (this) {
        MatchEventType.GOAL -> "⚽"
        MatchEventType.ASSIST -> "🅰️"
        MatchEventType.YELLOW_CARD -> "🟨"
        MatchEventType.RED_CARD -> "🟥"
    }

/** "⚽2 🟨 · 45 min": events plus the minutes in report mode or once completed. */
@Composable
private fun playerStats(player: TeamPlayer, state: MatchDetailState): String? {
    val match = state.match ?: return null
    if (player.isGuest) return null
    val events = match.events.filter { it.clubMemberId == player.id }
        .groupingBy { it.type }.eachCount().entries
        .joinToString(" ") { (type, count) -> if (count > 1) "${type.symbol}$count" else type.symbol }
    val showMinutes = state.reportMode || match.status == MatchStatus.COMPLETED
    val minutes = if (showMinutes) stringResource(Res.string.match_minutes_value, match.minutesOf(player.id)) else null
    return listOf(events, minutes).filter { !it.isNullOrBlank() }.joinToString(" · ").ifBlank { null }
}

@Composable
private fun ReportControls(player: TeamPlayer, state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    val match = state.match ?: return
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MatchEventType.entries.forEach { type ->
            TextButton(onClick = { onAction(MatchDetailAction.OnAddEventClick(player.id, type)) }, enabled = !state.isWorking) {
                Text(text = type.symbol)
            }
        }
        TextButton(onClick = { onAction(MatchDetailAction.OnEditMinutesClick(player.id)) }, enabled = !state.isWorking) {
            Text(text = stringResource(Res.string.match_minutes_value, match.minutesOf(player.id)), maxLines = 1, softWrap = false)
        }
    }
}

@Composable
private fun ScoreCard(state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    val match = state.match ?: return
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Official score: from goal events, or the manual one (APP-RN-10, BE-008 RN-E2)
            Text(
                text = stringResource(Res.string.match_score, match.teamAScore, match.teamBScore),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.extended.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (match.isManualScore) AssistChip(onClick = {}, label = { Text(text = stringResource(Res.string.match_score_manual)) })
        }
        if (state.canSetManualScore) {
            HintText(text = stringResource(Res.string.match_manual_score_hint))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onAction(MatchDetailAction.OnManualScoreClick) }, enabled = !state.isWorking) {
                    Text(text = stringResource(Res.string.match_manual_score))
                }
                if (match.isManualScore) {
                    TextButton(onClick = { onAction(MatchDetailAction.OnClearManualScore) }, enabled = !state.isWorking) {
                        Text(text = stringResource(Res.string.match_manual_score_clear))
                    }
                }
            }
        }
    }
}

@Composable
private fun EventsCard(state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    val match = state.match ?: return
    SectionCard {
        SectionTitle(text = stringResource(Res.string.match_events_title))
        if (match.events.isEmpty()) HintText(text = stringResource(Res.string.match_events_empty))
        match.events.forEach { event -> EventRow(event = event, state = state, onAction = onAction) }
    }
}

@Composable
private fun EventRow(event: MatchEventModel, state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    val player = state.player(event.clubMemberId, isGuest = false)
    val team = state.match?.teamOf(event.clubMemberId)?.name?.let { " ($it)" }.orEmpty()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = listOfNotNull(event.minute?.let { "$it'" }, event.type.symbol, stringResource(event.type.label)).joinToString(" ") +
                " · " + (player.name ?: stringResource(Res.string.former_member)) + team,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (state.canRecord) {
            IconButton(onClick = { onAction(MatchDetailAction.OnDeleteEvent(event)) }, enabled = !state.isWorking) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = stringResource(Res.string.match_event_delete))
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
}

@Composable
private fun CycleActions(state: MatchDetailState, onAction: (MatchDetailAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.canComplete) {
            SquadfyButton(
                text = stringResource(Res.string.match_complete),
                onClick = { onAction(MatchDetailAction.OnCompleteClick) },
                isLoading = state.isWorking,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (state.canReopen) {
            SquadfyButton(
                text = stringResource(Res.string.match_reopen),
                onClick = { onAction(MatchDetailAction.OnReopenClick) },
                style = SquadfyButtonStyle.SECONDARY,
                isLoading = state.isWorking,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (state.canCancel) {
            SquadfyButton(
                text = stringResource(Res.string.match_cancel),
                onClick = { onAction(MatchDetailAction.OnCancelMatchClick) },
                style = SquadfyButtonStyle.DESTRUCTIVE_SECONDARY,
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
                        Text(text = "${dialog.type.symbol} $name", style = MaterialTheme.typography.bodyLarge)
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
        IconButton(onClick = { onChange(-1) }) { Icon(imageVector = Icons.Outlined.Remove, contentDescription = stringResource(Res.string.match_decrease)) }
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = { onChange(1) }) { Icon(imageVector = Icons.Outlined.Add, contentDescription = stringResource(Res.string.match_increase)) }
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
