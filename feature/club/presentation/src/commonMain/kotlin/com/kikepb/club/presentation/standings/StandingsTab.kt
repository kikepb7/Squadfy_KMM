package com.kikepb.club.presentation.standings

import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.kikepb.club.presentation.mapper.toTri
import com.kikepb.core.designsystem.triangulacion.components.RankingRow
import com.kikepb.core.designsystem.triangulacion.components.TriChip
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriNotice
import com.kikepb.core.designsystem.triangulacion.components.TriNoticeTone
import com.kikepb.core.designsystem.triangulacion.components.TriTabs
import com.kikepb.core.designsystem.triangulacion.theme.Space
import squadfy_app.feature.club.presentation.generated.resources.standings_chip_assists
import squadfy_app.feature.club.presentation.generated.resources.standings_chip_goals
import squadfy_app.feature.club.presentation.generated.resources.standings_chip_matches
import squadfy_app.feature.club.presentation.generated.resources.standings_chip_minutes
import squadfy_app.feature.club.presentation.generated.resources.standings_chip_rating
import squadfy_app.feature.club.presentation.generated.resources.standings_chip_wins
import squadfy_app.feature.club.presentation.generated.resources.standings_unit_assists
import squadfy_app.feature.club.presentation.generated.resources.standings_unit_goals
import squadfy_app.feature.club.presentation.generated.resources.standings_unit_matches
import squadfy_app.feature.club.presentation.generated.resources.standings_unit_minutes
import squadfy_app.feature.club.presentation.generated.resources.standings_unit_wins
import com.kikepb.core.designsystem.components.loading.SquadfyLoadingIndicator
import squadfy_app.feature.club.presentation.generated.resources.standings_period_year
import squadfy_app.feature.club.presentation.generated.resources.standings_period_all
import squadfy_app.feature.club.presentation.generated.resources.standings_period_30d
import com.kikepb.club.domain.model.StatsPeriod
import com.kikepb.club.domain.model.SeasonModel
import com.kikepb.club.domain.model.SeasonStatus
import com.kikepb.core.designsystem.triangulacion.format.formatDayMonth
import squadfy_app.feature.club.presentation.generated.resources.season_chip
import squadfy_app.feature.club.presentation.generated.resources.season_closed_note
import squadfy_app.feature.club.presentation.generated.resources.season_starts_in
import squadfy_app.feature.club.presentation.generated.resources.season_status_active
import squadfy_app.feature.club.presentation.generated.resources.season_status_closed
import squadfy_app.feature.club.presentation.generated.resources.season_status_upcoming
import squadfy_app.feature.club.presentation.generated.resources.standings_period_season
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
import androidx.compose.foundation.lazy.items
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

/** Tabla A (spec 018): filter chips, then one ranking row per player with its value and last change. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsContent(club: ClubModel, state: StandingsState, onAction: (StandingsAction) -> Unit, onMemberClick: (String) -> Unit) {
    val formerMember = stringResource(Res.string.former_member)
    PullToRefreshBox(isRefreshing = state.isLoading, onRefresh = { onAction(StandingsAction.OnRefresh) }, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Space.s4),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Spec 020 APP-RN-S6: one chip per season, the one in progress first
            if (state.seasons.isNotEmpty()) {
                item(key = "seasons") {
                    SeasonChips(seasons = state.seasons, selectedId = state.selectedSeasonId, onSelected = { onAction(StandingsAction.OnSeasonSelected(it)) })
                }
            }
            item(key = "filters") { FilterChips(state = state, onAction = onAction) }
            if (state.mode == StandingsMode.STATS && state.isCurrentSeason) {
                // AC-015-07: period of the stats (the rating is cumulative and is never filtered)
                item(key = "period") {
                    PeriodChips(
                        selected = state.statsPeriod,
                        hasSeasons = state.seasons.isNotEmpty(),
                        onSelected = { onAction(StandingsAction.OnStatsPeriodSelected(it)) }
                    )
                }
            }
            if (state.isStale) item(key = "stale") { TriNotice(title = stringResource(Res.string.standings_offline), tone = TriNoticeTone.Warn, icon = TriIcon.Alert) }
            if (!state.hasLoaded) item(key = "loading") { SquadfyLoadingIndicator() }
            state.selectedSeason?.let { season ->
                when (season.status) {
                    // AC-020-02: «La temporada 2026/27 empieza el 1 sept.»
                    SeasonStatus.UPCOMING -> item(key = "pre-season") {
                        TriNotice(title = stringResource(Res.string.season_starts_in, season.name, formatDayMonth(season.startsOn)), icon = TriIcon.Calendar)
                    }
                    SeasonStatus.CLOSED -> item(key = "closed-season") {
                        TriNotice(title = stringResource(Res.string.season_closed_note, season.name), icon = TriIcon.Lock)
                    }
                    SeasonStatus.ACTIVE -> Unit
                }
            }
            if (state.isEmpty) item(key = "empty") { TriNotice(title = stringResource(Res.string.standings_empty), icon = TriIcon.Trophy) }
            when (state.mode) {
                StandingsMode.RATING -> items(items = state.ratings, key = { "r-${it.clubMemberId}" }) { entry ->
                    val member = state.members[entry.clubMemberId]
                    RankingRow(
                        position = entry.rank,
                        name = member?.username ?: formerMember,
                        value = entry.rating.toString(),
                        number = member?.shirtNumber,
                        playerPosition = member?.position.toTri(),
                        delta = state.lastMatchDeltas[entry.clubMemberId],
                        isMe = entry.clubMemberId == state.myMemberId,
                        provisional = entry.isProvisional,
                        imageUrl = member?.pictureUrl,
                        onClick = { onMemberClick(entry.clubMemberId) }
                    )
                }
                StandingsMode.STATS -> items(items = state.stats, key = { "s-${it.stats.clubMemberId}" }) { entry ->
                    val stats = entry.stats
                    val member = state.members[stats.clubMemberId]
                    val value = when (state.sortBy) {
                        StatsSortBy.GOALS -> stats.goals
                        StatsSortBy.ASSISTS -> stats.assists
                        StatsSortBy.MATCHES -> stats.matchesPlayed
                        StatsSortBy.MINUTES -> stats.minutesPlayed
                        StatsSortBy.WINS -> stats.wins
                    }
                    RankingRow(
                        position = entry.rank,
                        name = member?.username ?: formerMember,
                        value = value.toString(),
                        number = member?.shirtNumber,
                        playerPosition = member?.position.toTri(),
                        unit = stringResource(state.sortBy.unit),
                        isMe = stats.clubMemberId == state.myMemberId,
                        imageUrl = member?.pictureUrl,
                        onClick = { onMemberClick(stats.clubMemberId) }
                    )
                }
            }
        }
    }
}

private val StatsSortBy.chip: StringResource
    get() = when (this) {
        StatsSortBy.GOALS -> Res.string.standings_chip_goals
        StatsSortBy.ASSISTS -> Res.string.standings_chip_assists
        StatsSortBy.MATCHES -> Res.string.standings_chip_matches
        StatsSortBy.MINUTES -> Res.string.standings_chip_minutes
        StatsSortBy.WINS -> Res.string.standings_chip_wins
    }

private val StatsSortBy.unit: StringResource
    get() = when (this) {
        StatsSortBy.GOALS -> Res.string.standings_unit_goals
        StatsSortBy.ASSISTS -> Res.string.standings_unit_assists
        StatsSortBy.MATCHES -> Res.string.standings_unit_matches
        StatsSortBy.MINUTES -> Res.string.standings_unit_minutes
        StatsSortBy.WINS -> Res.string.standings_unit_wins
    }

/** With seasons (spec 020), «Todo» is the whole selected season. */
private fun StatsPeriod.label(hasSeasons: Boolean): StringResource = when (this) {
    StatsPeriod.ALL_TIME -> if (hasSeasons) Res.string.standings_period_season else Res.string.standings_period_all
    StatsPeriod.THIS_YEAR -> Res.string.standings_period_year
    StatsPeriod.LAST_30_DAYS -> Res.string.standings_period_30d
}

private val SeasonStatus.label: StringResource
    get() = when (this) {
        SeasonStatus.ACTIVE -> Res.string.season_status_active
        SeasonStatus.UPCOMING -> Res.string.season_status_upcoming
        SeasonStatus.CLOSED -> Res.string.season_status_closed
    }

/** «2026/27 · en curso», «2025/26 · cerrada». */
@Composable
private fun SeasonChips(seasons: List<SeasonModel>, selectedId: String?, onSelected: (String) -> Unit) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        seasons.forEach { season ->
            TriChip(
                text = stringResource(Res.string.season_chip, season.name, stringResource(season.status.label)),
                selected = season.id == selectedId,
                onClick = { onSelected(season.id) },
                icon = if (season.status == SeasonStatus.ACTIVE) TriIcon.Calendar else null
            )
        }
    }
}

/** Rating, Goles, Asist., PJ… : the first is the rating table, the others the stats sorted by that column. */
@Composable
private fun FilterChips(state: StandingsState, onAction: (StandingsAction) -> Unit) {
    val order = listOf(StatsSortBy.GOALS, StatsSortBy.ASSISTS, StatsSortBy.MATCHES, StatsSortBy.WINS, StatsSortBy.MINUTES)
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TriChip(
            text = stringResource(Res.string.standings_chip_rating),
            selected = state.mode == StandingsMode.RATING,
            onClick = { onAction(StandingsAction.OnModeSelected(StandingsMode.RATING)) },
            icon = TriIcon.Trophy
        )
        order.forEach { sortBy ->
            TriChip(
                text = stringResource(sortBy.chip),
                selected = state.mode == StandingsMode.STATS && state.sortBy == sortBy,
                onClick = {
                    onAction(StandingsAction.OnModeSelected(StandingsMode.STATS))
                    onAction(StandingsAction.OnSortSelected(sortBy))
                },
                icon = if (sortBy == StatsSortBy.GOALS) TriIcon.Ball else null
            )
        }
    }
}

@Composable
private fun PeriodChips(selected: StatsPeriod, hasSeasons: Boolean, onSelected: (StatsPeriod) -> Unit) {
    TriTabs(
        items = StatsPeriod.entries.map { stringResource(it.label(hasSeasons)) },
        selectedIndex = StatsPeriod.entries.indexOf(selected),
        onSelect = { onSelected(StatsPeriod.entries[it]) }
    )
}

// Previews (spec 018): Tiza and Noche with the default state, no sample data
private val blankClub = ClubModel(id = "", name = "", description = null, clubLogoUrl = null, ownerId = "", invitationCode = "", maxMembers = null, membersCount = 0)

@Preview(name = "Tabla · rating · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun StandingsRatingTizaPreview() = ScreenPreview { StandingsContent(club = blankClub, state = StandingsState(), onAction = {}, onMemberClick = {}) }

@Preview(name = "Tabla · rating · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun StandingsRatingNochePreview() = ScreenPreview(darkTheme = true) { StandingsContent(club = blankClub, state = StandingsState(), onAction = {}, onMemberClick = {}) }

@Preview(name = "Tabla · goles · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun StandingsGoalsTizaPreview() = ScreenPreview { StandingsContent(club = blankClub, state = StandingsState(mode = StandingsMode.STATS), onAction = {}, onMemberClick = {}) }

@Preview(name = "Tabla · goles · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun StandingsGoalsNochePreview() = ScreenPreview(darkTheme = true) { StandingsContent(club = blankClub, state = StandingsState(mode = StandingsMode.STATS), onAction = {}, onMemberClick = {}) }
