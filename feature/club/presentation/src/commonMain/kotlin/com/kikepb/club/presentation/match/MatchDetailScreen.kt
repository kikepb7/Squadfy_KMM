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
import androidx.compose.material3.AssistChip
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.common_cancel
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
            MatchDetailEvent.Close -> onBackClick()
        }
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
                matchTeamsSection(state = state, onAction = onAction)
                state.balance?.takeIf { state.isManager && state.editing == null }?.let { balance ->
                    item(key = "balance") { BalancePanel(balance = balance, state = state) }
                }
            }
        }
    }

    if (state.dialog == MatchDetailDialog.ConfirmRedraw) {
        val dismiss = { onAction(MatchDetailAction.OnDismissDialog) }
        SquadfyDestructiveConfirmationDialog(
            title = stringResource(Res.string.match_redraw_title),
            description = stringResource(Res.string.match_redraw_description),
            confirmButtonText = stringResource(Res.string.match_redraw),
            cancelButtonText = stringResource(Res.string.common_cancel),
            onConfirmClick = { onAction(MatchDetailAction.OnConfirmRedraw) },
            onCancelClick = dismiss,
            onDismiss = dismiss
        )
    }
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
                onClick = { onAction(MatchDetailAction.OnMovePlayer(player.id)) }
            )
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
private fun PlayerRow(player: TeamPlayer, highlighted: Boolean, editing: Boolean, onClick: () -> Unit) {
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
