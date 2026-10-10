package com.kikepb.club.presentation.memberdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.presentation.mapper.label
import com.kikepb.club.presentation.mapper.toTri
import com.kikepb.core.designsystem.triangulacion.components.CutLg
import com.kikepb.core.designsystem.triangulacion.components.PosChip
import com.kikepb.core.designsystem.triangulacion.components.RatingDelta
import com.kikepb.core.designsystem.triangulacion.components.RatingSparkline
import com.kikepb.core.designsystem.triangulacion.components.TriAvatar
import com.kikepb.core.designsystem.triangulacion.components.TriCaps
import com.kikepb.core.designsystem.triangulacion.components.TriCard
import com.kikepb.core.designsystem.triangulacion.components.TriSectionTitle
import com.kikepb.core.designsystem.triangulacion.components.TriTag
import com.kikepb.core.designsystem.triangulacion.components.TriTagTone
import com.kikepb.core.designsystem.triangulacion.format.TriDates
import com.kikepb.core.designsystem.triangulacion.format.formatDayMonth
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.designsystem.triangulacion.theme.tabular
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.core.designsystem.generated.resources.Res as DesignRes
import squadfy_app.core.designsystem.generated.resources.tri_provisional
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.member_evolution_caps
import squadfy_app.feature.club.presentation.generated.resources.member_rating_caps
import squadfy_app.feature.club.presentation.generated.resources.member_rating_caps_rank
import squadfy_app.feature.club.presentation.generated.resources.member_recent_title
import squadfy_app.feature.club.presentation.generated.resources.member_stat_assists
import squadfy_app.feature.club.presentation.generated.resources.member_stat_goals
import squadfy_app.feature.club.presentation.generated.resources.member_stat_matches
import squadfy_app.feature.club.presentation.generated.resources.member_stat_minutes
import squadfy_app.feature.club.presentation.generated.resources.member_stat_record
import squadfy_app.feature.club.presentation.generated.resources.member_stat_yellows

/** Ficha del jugador (spec 018, screen 09): hero with the rating, evolution, stats grid and recent matches. */
@Composable
fun MemberStatsSection(member: ClubMemberModel, viewModel: MemberStatsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MemberStatsContent(member = member, state = state)
}

/** Stateless body of the player card, also used by the previews. */
@Composable
fun MemberStatsContent(member: ClubMemberModel, state: MemberStatsState) {
    Column(verticalArrangement = Arrangement.spacedBy(Space.s3)) {
        PlayerHero(member = member, state = state)
        if (state.evolution.size >= 2) {
            TriCard(modifier = Modifier.fillMaxWidth(), contentPadding = Space.s4) {
                TriCaps(text = stringResource(Res.string.member_evolution_caps))
                RatingSparkline(values = state.evolution, modifier = Modifier.padding(top = Space.s3))
            }
        }
        state.stats?.let { stats ->
            StatsGrid(
                listOf(
                    stats.matchesPlayed.toString() to stringResource(Res.string.member_stat_matches),
                    "${stats.wins}·${stats.draws}·${stats.losses}" to stringResource(Res.string.member_stat_record),
                    stats.goals.toString() to stringResource(Res.string.member_stat_goals),
                    stats.assists.toString() to stringResource(Res.string.member_stat_assists),
                    stats.minutesPlayed.toString() to stringResource(Res.string.member_stat_minutes),
                    stats.yellowCards.toString() to stringResource(Res.string.member_stat_yellows)
                )
            )
        }
        if (state.recentChanges.isNotEmpty()) {
            TriSectionTitle(text = stringResource(Res.string.member_recent_title), modifier = Modifier.padding(top = Space.s3))
            // AC-008-08: newest first, each match with its rating change
            state.recentChanges.forEach { change ->
                val local = TriDates.local(change.scheduledAt, null)
                TriCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${stringResource(TriDates.shortDay(local.dayOfWeek))} ${formatDayMonth(local.date)}",
                            style = Triangulacion.type.label.copy(fontSize = Triangulacion.type.body.fontSize),
                            color = Triangulacion.colors.ink,
                            modifier = Modifier.weight(1f)
                        )
                        RatingDelta(change.change)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerHero(member: ClubMemberModel, state: MemberStatsState) {
    val colors = Triangulacion.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutLg)
            .background(colors.surfaceInverse)
            .drawBehind {
                val cut = Radius.cutLg.toPx()
                drawPath(Path().apply { moveTo(size.width - cut, 0f); lineTo(size.width, cut); lineTo(size.width - cut, cut); close() }, colors.volt)
            }
            .padding(Space.s5),
        verticalArrangement = Arrangement.spacedBy(Space.s4)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            TriAvatar(name = member.username, imageUrl = member.pictureUrl, size = 64.dp, number = member.shirtNumber, onInverse = true)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Text(text = member.username, style = Triangulacion.type.displayMd, color = colors.onInverse, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    member.position.toTri()?.let { PosChip(it, longName = true, highlighted = true) }
                    if (member.role != ClubMemberRole.PLAYER) TriTag(text = stringResource(member.role.label), tone = TriTagTone.Neutral)
                }
            }
        }
        state.rating?.let { rating ->
            Column {
                TriCaps(
                    text = if (state.rank != null && state.totalPlayers != null) stringResource(Res.string.member_rating_caps_rank, state.rank, state.totalPlayers)
                    else stringResource(Res.string.member_rating_caps),
                    color = colors.onInverseMuted
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = rating.toString(), style = Triangulacion.type.statXl, color = colors.onInverse, modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (state.isProvisional) TriTag(text = stringResource(DesignRes.string.tri_provisional), tone = TriTagTone.Warn)
                        state.recentChanges.firstOrNull()?.let { RatingDelta(it.change, onInverse = true) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(cells: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        cells.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (value, label) ->
                    TriCard(modifier = Modifier.weight(1f), contentPadding = 14.dp) {
                        Text(text = value, style = Triangulacion.type.displayMd.tabular, color = Triangulacion.colors.ink, maxLines = 1)
                        TriCaps(text = label)
                    }
                }
                repeat(3 - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}
