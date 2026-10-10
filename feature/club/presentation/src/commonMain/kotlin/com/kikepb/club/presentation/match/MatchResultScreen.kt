package com.kikepb.club.presentation.match

import com.kikepb.club.presentation.mapper.matchFormatLabel
import com.kikepb.club.presentation.preview.ClubSamples
import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.Team
import com.kikepb.club.presentation.mapper.label
import com.kikepb.club.presentation.mapper.toTri
import com.kikepb.core.designsystem.components.loading.SquadfyLoadingIndicator
import com.kikepb.core.designsystem.components.systembars.StatusBarIconsEffect
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBarBackButton
import com.kikepb.core.designsystem.triangulacion.components.CutLg
import com.kikepb.core.designsystem.triangulacion.components.RatingDelta
import com.kikepb.core.designsystem.triangulacion.components.TriAvatar
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriButtonVariant
import com.kikepb.core.designsystem.triangulacion.components.TriCaps
import com.kikepb.core.designsystem.triangulacion.components.TriCard
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriNotice
import com.kikepb.core.designsystem.triangulacion.components.TriRoundIconButton
import com.kikepb.core.designsystem.triangulacion.components.TriSectionTitle
import com.kikepb.core.designsystem.triangulacion.components.TriTag
import com.kikepb.core.designsystem.triangulacion.components.TriTagTone
import com.kikepb.core.designsystem.triangulacion.components.TriangulationArt
import com.kikepb.core.designsystem.triangulacion.components.teamName
import com.kikepb.core.designsystem.triangulacion.format.TriDates
import com.kikepb.core.designsystem.triangulacion.format.formatDayMonth
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.core.designsystem.generated.resources.Res as DesignRes
import squadfy_app.core.designsystem.generated.resources.squadfy_topbar_back
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.draw_balance
import squadfy_app.feature.club.presentation.generated.resources.draw_balance_even
import squadfy_app.feature.club.presentation.generated.resources.draw_balance_off
import squadfy_app.feature.club.presentation.generated.resources.draw_balance_slight
import squadfy_app.feature.club.presentation.generated.resources.draw_body_default
import squadfy_app.feature.club.presentation.generated.resources.draw_caps
import squadfy_app.feature.club.presentation.generated.resources.draw_not_yet
import squadfy_app.feature.club.presentation.generated.resources.draw_published
import squadfy_app.feature.club.presentation.generated.resources.draw_view_teams
import squadfy_app.feature.club.presentation.generated.resources.draw_you_play_in
import squadfy_app.feature.club.presentation.generated.resources.former_member
import squadfy_app.feature.club.presentation.generated.resources.result_caps
import squadfy_app.feature.club.presentation.generated.resources.result_changes
import squadfy_app.feature.club.presentation.generated.resources.result_draw
import squadfy_app.feature.club.presentation.generated.resources.result_loss
import squadfy_app.feature.club.presentation.generated.resources.result_pending
import squadfy_app.feature.club.presentation.generated.resources.result_title
import squadfy_app.feature.club.presentation.generated.resources.result_win
import squadfy_app.feature.club.presentation.generated.resources.result_your_change
import squadfy_app.feature.club.presentation.generated.resources.result_your_rating
import kotlin.math.abs

/** Resultado (spec 018, screen 16): the closed match, my change and everyone's rating change. */
@Composable
fun MatchResultRoot(
    onBackClick: () -> Unit,
    viewModel: MatchDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.onAction(MatchDetailAction.OnRefresh)
        onPauseOrDispose { }
    }
    MatchResultScreen(state = state, onBackClick = onBackClick)
}

@Composable
fun MatchResultScreen(state: MatchDetailState, onBackClick: () -> Unit) {
    val colors = Triangulacion.colors
    val formerMember = stringResource(Res.string.former_member)
    Scaffold(
        containerColor = colors.surface100,
        topBar = {
            SquadfyTopBar(
                title = { Text(text = stringResource(Res.string.result_title), style = Triangulacion.type.displayMd, color = colors.ink) },
                navigationIcon = { SquadfyTopBarBackButton(onClick = onBackClick) }
            )
        }
    ) { padding ->
        val match = state.match
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Space.s4),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (match == null) {
                item(key = "loading") { SquadfyLoadingIndicator() }
                return@LazyColumn
            }
            item(key = "hero") { ResultHero(state) }
            if (match.status != MatchStatus.COMPLETED) {
                item(key = "pending") { TriNotice(title = stringResource(Res.string.result_pending), icon = TriIcon.Clock) }
                return@LazyColumn
            }
            val myChange = state.myMemberId?.let { match.ratingChanges[it] }
            if (myChange != null) {
                item(key = "mine-title") { TriSectionTitle(text = stringResource(Res.string.result_your_rating), modifier = Modifier.padding(top = Space.s3)) }
                item(key = "mine") {
                    TriCard(modifier = Modifier.fillMaxWidth(), contentPadding = Space.s4) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = stringResource(Res.string.result_your_change), style = Triangulacion.type.body, color = colors.inkMuted, modifier = Modifier.weight(1f))
                            RatingDelta(myChange)
                        }
                    }
                }
            }
            val changes = match.ratingChanges.entries.sortedByDescending { it.value }
            if (changes.isNotEmpty()) {
                item(key = "changes-title") { TriSectionTitle(text = stringResource(Res.string.result_changes), modifier = Modifier.padding(top = Space.s3)) }
                items(items = changes, key = { "c-${it.key}" }) { (memberId, change) ->
                    val player = state.player(memberId, isGuest = false)
                    val isMe = memberId == state.myMemberId
                    TriCard(modifier = Modifier.fillMaxWidth(), highlighted = isMe, contentPadding = 12.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TriAvatar(name = player.name ?: formerMember, imageUrl = player.profilePictureUrl, size = 40.dp, isMe = isMe)
                            Text(text = player.name ?: formerMember, style = Triangulacion.type.title, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            RatingDelta(change)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultHero(state: MatchDetailState) {
    val colors = Triangulacion.colors
    val match = state.match ?: return
    val local = TriDates.local(match.scheduledAt, state.timeZoneId)
    val myTeam = state.myTeam
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Space.s3),
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutLg)
            .background(colors.surfaceInverse)
            .drawBehind {
                val cut = Radius.cutLg.toPx()
                drawPath(Path().apply { moveTo(size.width - cut, 0f); lineTo(size.width, cut); lineTo(size.width - cut, cut); close() }, colors.volt)
            }
            .padding(vertical = Space.s6, horizontal = Space.s5)
    ) {
        TriCaps(
            text = stringResource(
                Res.string.result_caps,
                "${stringResource(TriDates.shortDay(local.dayOfWeek))} ${formatDayMonth(local.date)}",
                state.announcement?.maxPlayers?.let { matchFormatLabel(it) } ?: state.format?.let { stringResource(it.label) } ?: stringResource(Res.string.result_title)
            ),
            color = colors.onInverseMuted
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.s4)) {
            Text(text = match.teamAScore.toString(), style = Triangulacion.type.statXl.copy(fontSize = Triangulacion.type.statXl.fontSize * 1.3f), color = colors.onInverse)
            androidx.compose.foundation.layout.Box(Modifier.size(14.dp).clip(CircleShape).background(colors.volt))
            Text(text = match.teamBScore.toString(), style = Triangulacion.type.statXl.copy(fontSize = Triangulacion.type.statXl.fontSize * 1.3f), color = colors.onInverse)
        }
        if (match.status == MatchStatus.COMPLETED && myTeam != null) {
            val (mine, theirs) = if (myTeam == Team.A) match.teamAScore to match.teamBScore else match.teamBScore to match.teamAScore
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when {
                    mine > theirs -> TriTag(text = stringResource(Res.string.result_win))
                    mine < theirs -> TriTag(text = stringResource(Res.string.result_loss), tone = TriTagTone.Danger)
                    else -> TriTag(text = stringResource(Res.string.result_draw), tone = TriTagTone.Neutral)
                }
                TriTag(text = teamName(myTeam.toTri()), tone = TriTagTone.Neutral)
            }
        }
    }
}

// Previews (spec 018): Tiza and Noche with the data of the mockups

// Previews (spec 018): Tiza and Noche with the data of the mockups

@Preview(name = "16 Resultado · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ResultTizaPreview() = ScreenPreview { MatchResultScreen(state = ClubSamples.matchDetail(MatchStatus.COMPLETED), onBackClick = {}) }

@Preview(name = "16 Resultado · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ResultNochePreview() = ScreenPreview(darkTheme = true) { MatchResultScreen(state = ClubSamples.matchDetail(MatchStatus.COMPLETED), onBackClick = {}) }
