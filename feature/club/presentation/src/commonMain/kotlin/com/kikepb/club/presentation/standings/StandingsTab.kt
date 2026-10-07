package com.kikepb.club.presentation.standings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.StatsSortBy
import com.kikepb.club.presentation.components.HintText
import com.kikepb.club.presentation.components.SectionCard
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.former_member
import squadfy_app.feature.club.presentation.generated.resources.standings_empty
import squadfy_app.feature.club.presentation.generated.resources.standings_header_assists
import squadfy_app.feature.club.presentation.generated.resources.standings_header_draws
import squadfy_app.feature.club.presentation.generated.resources.standings_header_goals
import squadfy_app.feature.club.presentation.generated.resources.standings_header_losses
import squadfy_app.feature.club.presentation.generated.resources.standings_header_matches
import squadfy_app.feature.club.presentation.generated.resources.standings_header_minutes
import squadfy_app.feature.club.presentation.generated.resources.standings_header_player
import squadfy_app.feature.club.presentation.generated.resources.standings_header_rating
import squadfy_app.feature.club.presentation.generated.resources.standings_header_wins
import squadfy_app.feature.club.presentation.generated.resources.standings_my_position
import squadfy_app.feature.club.presentation.generated.resources.standings_offline
import squadfy_app.feature.club.presentation.generated.resources.standings_provisional
import squadfy_app.feature.club.presentation.generated.resources.standings_rating
import squadfy_app.feature.club.presentation.generated.resources.standings_sort_assists
import squadfy_app.feature.club.presentation.generated.resources.standings_sort_goals
import squadfy_app.feature.club.presentation.generated.resources.standings_sort_matches
import squadfy_app.feature.club.presentation.generated.resources.standings_sort_minutes
import squadfy_app.feature.club.presentation.generated.resources.standings_sort_wins
import squadfy_app.feature.club.presentation.generated.resources.standings_stats

/** "Classification" tab: rating and stats standings from the backend (spec 008). */
@Composable
fun StandingsTab(
    club: ClubModel,
    onMemberClick: (String) -> Unit,
    viewModel: StandingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // A match completed or reopened elsewhere shows up when coming back (AC-008-05)
    LifecycleResumeEffect(Unit) {
        viewModel.onAction(StandingsAction.OnRefresh)
        onPauseOrDispose { }
    }
    StandingsContent(club = club, state = state, onAction = viewModel::onAction, onMemberClick = onMemberClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsContent(club: ClubModel, state: StandingsState, onAction: (StandingsAction) -> Unit, onMemberClick: (String) -> Unit) {
    PullToRefreshBox(isRefreshing = state.isLoading, onRefresh = { onAction(StandingsAction.OnRefresh) }, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isStale) item(key = "stale") { HintText(text = stringResource(Res.string.standings_offline)) }
            item(key = "mode") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    StandingsMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = state.mode == mode,
                            onClick = { onAction(StandingsAction.OnModeSelected(mode)) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = StandingsMode.entries.size)
                        ) {
                            Text(text = stringResource(if (mode == StandingsMode.RATING) Res.string.standings_rating else Res.string.standings_stats))
                        }
                    }
                }
            }
            if (state.isEmpty) {
                item(key = "empty") { SectionCard { HintText(text = stringResource(Res.string.standings_empty)) } }
            }
            when (state.mode) {
                StandingsMode.RATING -> {
                    state.myRating?.let { mine ->
                        item(key = "mine") {
                            SectionCard {
                                Text(
                                    text = stringResource(Res.string.standings_my_position, mine.rank, mine.totalPlayers, mine.rating),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    item(key = "ratings") { RatingTable(state = state, onMemberClick = onMemberClick) }
                }
                StandingsMode.STATS -> {
                    item(key = "sort") { SortChips(selected = state.sortBy, onSelected = { onAction(StandingsAction.OnSortSelected(it)) }) }
                    item(key = "stats") { StatsTable(state = state, onMemberClick = onMemberClick) }
                }
            }
        }
    }
}

private val StatsSortBy.label: StringResource
    get() = when (this) {
        StatsSortBy.GOALS -> Res.string.standings_sort_goals
        StatsSortBy.ASSISTS -> Res.string.standings_sort_assists
        StatsSortBy.MATCHES -> Res.string.standings_sort_matches
        StatsSortBy.MINUTES -> Res.string.standings_sort_minutes
        StatsSortBy.WINS -> Res.string.standings_sort_wins
    }

@Composable
private fun SortChips(selected: StatsSortBy, onSelected: (StatsSortBy) -> Unit) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatsSortBy.entries.forEach { sortBy ->
            FilterChip(selected = sortBy == selected, onClick = { onSelected(sortBy) }, label = { Text(text = stringResource(sortBy.label)) })
        }
    }
}

@Composable
private fun RatingTable(state: StandingsState, onMemberClick: (String) -> Unit) {
    val provisional = stringResource(Res.string.standings_provisional)
    TableCard {
        TableRow(highlighted = false, header = true, onClick = null) {
            Cell("#", RANK_WIDTH, header = true)
            Cell(stringResource(Res.string.standings_header_player), null, header = true)
            Cell(stringResource(Res.string.standings_header_rating), 64.dp, header = true)
            Cell(stringResource(Res.string.standings_header_matches), NUMBER_WIDTH, header = true)
        }
        state.ratings.forEach { entry ->
            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            TableRow(highlighted = entry.clubMemberId == state.myMemberId, onClick = { onMemberClick(entry.clubMemberId) }) {
                Cell(entry.rank.toString(), RANK_WIDTH)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.members[entry.clubMemberId]?.username ?: stringResource(Res.string.former_member),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (entry.isProvisional) HintText(text = provisional)
                }
                Cell(entry.rating.toString(), 64.dp)
                Cell(entry.matchesRated.toString(), NUMBER_WIDTH)
            }
        }
    }
}

@Composable
private fun StatsTable(state: StandingsState, onMemberClick: (String) -> Unit) {
    // Fixed player column plus a horizontally scrollable block of numbers (AC-008-03)
    val scroll = rememberScrollState()
    val headers = listOf(
        stringResource(Res.string.standings_header_matches), stringResource(Res.string.standings_header_wins),
        stringResource(Res.string.standings_header_draws), stringResource(Res.string.standings_header_losses),
        stringResource(Res.string.standings_header_goals), stringResource(Res.string.standings_header_assists),
        "🟨", "🟥", stringResource(Res.string.standings_header_minutes)
    )
    TableCard {
        TableRow(highlighted = false, header = true, onClick = null) {
            Cell("#", RANK_WIDTH, header = true)
            Cell(stringResource(Res.string.standings_header_player), PLAYER_WIDTH, header = true)
            Row(modifier = Modifier.horizontalScroll(scroll)) { headers.forEach { Cell(it, STAT_WIDTH, header = true) } }
        }
        state.stats.forEach { entry ->
            val stats = entry.stats
            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            TableRow(highlighted = stats.clubMemberId == state.myMemberId, onClick = { onMemberClick(stats.clubMemberId) }) {
                Cell(entry.rank.toString(), RANK_WIDTH)
                Cell(state.members[stats.clubMemberId]?.username ?: stringResource(Res.string.former_member), PLAYER_WIDTH)
                Row(modifier = Modifier.horizontalScroll(scroll)) {
                    listOf(
                        stats.matchesPlayed, stats.wins, stats.draws, stats.losses, stats.goals,
                        stats.assists, stats.yellowCards, stats.redCards, stats.minutesPlayed
                    ).forEach { Cell(it.toString(), STAT_WIDTH) }
                }
            }
        }
    }
}

@Composable
private fun TableCard(content: @Composable () -> Unit) {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column { content() }
    }
}

@Composable
private fun TableRow(highlighted: Boolean, onClick: (() -> Unit)?, header: Boolean = false, content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    val background = when {
        header -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        highlighted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Cell(text: String, width: Dp?, header: Boolean = false) {
    Text(
        text = text,
        style = if (header) MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
        color = if (header) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.extended.textPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = if (width != null) Modifier.width(width) else Modifier.weight(1f)
    )
}

private val RANK_WIDTH = 32.dp
private val NUMBER_WIDTH = 40.dp
private val PLAYER_WIDTH = 110.dp
private val STAT_WIDTH = 52.dp
