package com.kikepb.club.presentation.match

import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.presentation.mapper.toTri
import com.kikepb.core.designsystem.components.loading.SquadfyLoadingIndicator
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBarBackButton
import com.kikepb.core.designsystem.triangulacion.components.Pitch
import com.kikepb.core.designsystem.triangulacion.components.PitchPlayer
import com.kikepb.core.designsystem.triangulacion.components.TeamBalance
import com.kikepb.core.designsystem.triangulacion.components.TriCaps
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriNotice
import com.kikepb.core.designsystem.triangulacion.format.TriDates
import com.kikepb.core.designsystem.triangulacion.format.formatClubTime
import com.kikepb.core.designsystem.triangulacion.format.formatLongDayCaps
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.draw_not_yet
import squadfy_app.feature.club.presentation.generated.resources.former_member
import squadfy_app.feature.club.presentation.generated.resources.pitch_title

/**
 * Campo (spec 018, screen 06-campo): both teams of a match on the pitch, each player in his position, the passes
 * between close team-mates and the balance below. Same data as the match detail (its ViewModel).
 */
@Composable
fun MatchPitchRoot(onBackClick: () -> Unit, viewModel: MatchDetailViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.onAction(MatchDetailAction.OnRefresh)
        onPauseOrDispose { }
    }
    MatchPitchScreen(state = state, onBackClick = onBackClick)
}

/** Team players of the match detail as pitch players (first name only, like the design). */
fun List<TeamPlayer>.toPitchPlayers(formerMember: String): List<PitchPlayer> = map { player ->
    val name = player.name ?: formerMember
    PitchPlayer(
        id = player.id,
        name = name.substringBefore(' '),
        number = player.shirtNumber,
        position = player.position.toTri(),
        isMe = player.isMe,
        isGuest = player.isGuest
    )
}

@Composable
fun MatchPitchScreen(state: MatchDetailState, onBackClick: () -> Unit) {
    val colors = Triangulacion.colors
    val formerMember = stringResource(Res.string.former_member)
    Scaffold(
        containerColor = colors.surface100,
        topBar = {
            SquadfyTopBar(
                title = {
                    Column {
                        Text(text = stringResource(Res.string.pitch_title), style = Triangulacion.type.displayMd, color = colors.ink)
                        state.match?.let { match ->
                            val local = TriDates.local(match.scheduledAt, state.timeZoneId)
                            TriCaps(text = "${formatLongDayCaps(local.date)} · ${formatClubTime(match.scheduledAt, state.timeZoneId)}")
                        }
                    }
                },
                navigationIcon = { SquadfyTopBarBackButton(onClick = onBackClick) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.s4, vertical = Space.s2),
            verticalArrangement = Arrangement.spacedBy(Space.s6)
        ) {
            val match = state.match
            when {
                match == null -> SquadfyLoadingIndicator()
                !match.hasTeams -> TriNotice(title = stringResource(Res.string.draw_not_yet), icon = TriIcon.Shuffle)
                else -> {
                    Pitch(teamA = state.teamA.toPitchPlayers(formerMember), teamB = state.teamB.toPitchPlayers(formerMember))
                    state.balance?.let { balance ->
                        TeamBalance(
                            ratingA = balance.teamA.averageRating,
                            ratingB = balance.teamB.averageRating,
                            expectedA = balance.teamAExpectedScore.toFloat(),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Previews (spec 018): Tiza and Noche with the default state, no sample data

@Preview(name = "06 Campo · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun PitchTizaPreview() = ScreenPreview { MatchPitchScreen(state = MatchDetailState(), onBackClick = {}) }

@Preview(name = "06 Campo · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun PitchNochePreview() = ScreenPreview(darkTheme = true) { MatchPitchScreen(state = MatchDetailState(), onBackClick = {}) }
