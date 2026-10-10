package com.kikepb.club.presentation.teams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.MatchGuestModel
import com.kikepb.club.presentation.components.LoadErrorCard
import com.kikepb.club.presentation.mapper.toTri
import com.kikepb.core.designsystem.components.loading.SquadfyLoadingIndicator
import com.kikepb.core.designsystem.triangulacion.components.Pitch
import com.kikepb.core.designsystem.triangulacion.components.PitchPlayer
import com.kikepb.core.designsystem.triangulacion.components.TeamBadge
import com.kikepb.core.designsystem.triangulacion.components.TeamBalance
import com.kikepb.core.designsystem.triangulacion.components.TriAvatar
import com.kikepb.core.designsystem.triangulacion.components.TriCaps
import com.kikepb.core.designsystem.triangulacion.components.TriCard
import com.kikepb.core.designsystem.triangulacion.components.TriTabs
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriNotice
import com.kikepb.core.designsystem.triangulacion.components.TriNoticeTone
import com.kikepb.core.designsystem.triangulacion.components.TriTeam
import com.kikepb.core.designsystem.triangulacion.components.TriTextButton
import com.kikepb.core.designsystem.triangulacion.components.teamName
import com.kikepb.core.designsystem.triangulacion.format.formatMatchDate
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.designsystem.triangulacion.theme.tabular
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.core.designsystem.generated.resources.Res as DesignRes
import squadfy_app.core.designsystem.generated.resources.tri_team_a_caps
import squadfy_app.core.designsystem.generated.resources.tri_team_b_caps
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.former_member
import squadfy_app.feature.club.presentation.generated.resources.teams_no_match
import squadfy_app.feature.club.presentation.generated.resources.teams_not_yet
import squadfy_app.feature.club.presentation.generated.resources.teams_not_yet_body
import squadfy_app.feature.club.presentation.generated.resources.teams_open_match
import squadfy_app.feature.club.presentation.generated.resources.teams_published_title
import squadfy_app.feature.club.presentation.generated.resources.teams_view_list
import squadfy_app.feature.club.presentation.generated.resources.teams_view_pitch
import squadfy_app.feature.club.presentation.generated.resources.teams_you_play_in

@Composable
fun TeamsTab(
    onOpenMatch: (matchId: String) -> Unit,
    viewModel: TeamsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.onAction(TeamsAction.OnResume)
        onPauseOrDispose { }
    }
    TeamsContent(state = state, onAction = viewModel::onAction, onOpenMatch = onOpenMatch)
}

/** A team member or guest as shown in this tab. */
private data class TeamPlayer(val id: String, val name: String, val number: Int?, val player: PitchPlayer)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsContent(state: TeamsState, onAction: (TeamsAction) -> Unit, onOpenMatch: (String) -> Unit) {
    val formerMember = stringResource(Res.string.former_member)
    val match = state.match
    fun playersOf(memberIds: List<String>, guests: List<MatchGuestModel>): List<TeamPlayer> =
        memberIds.map { id ->
            val member = state.members[id]
            val name = member?.username ?: formerMember
            TeamPlayer(id, name, member?.shirtNumber, PitchPlayer(id, name.substringBefore(' '), member?.shirtNumber, member?.position.toTri(), isMe = id == state.myMemberId))
        } + guests.map { guest ->
            TeamPlayer(guest.guestId, guest.name, null, PitchPlayer(guest.guestId, guest.name.substringBefore(' '), null, guest.position.toTri(), isGuest = true))
        }
    val teamA = match?.let { playersOf(it.teamA, it.teamAGuests) }.orEmpty()
    val teamB = match?.let { playersOf(it.teamB, it.teamBGuests) }.orEmpty()
    val myTeam = state.myMemberId?.let { match?.teamOf(it) }

    PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = { onAction(TeamsAction.OnRefresh) }, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Space.s4),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                !state.hasLoaded && state.loadFailed -> item(key = "error") { LoadErrorCard(onRetry = { onAction(TeamsAction.OnRefresh) }) }
                !state.hasLoaded -> item(key = "loading") { SquadfyLoadingIndicator() }
                !state.hasMatch -> item(key = "no-match") {
                    TriNotice(title = stringResource(Res.string.teams_not_yet), body = stringResource(Res.string.teams_no_match))
                }
                !state.hasTeams -> item(key = "not-yet") {
                    TriNotice(
                        title = stringResource(Res.string.teams_not_yet),
                        body = state.drawAt?.let { stringResource(Res.string.teams_not_yet_body, formatMatchDate(it, state.timeZoneId)) },
                        icon = TriIcon.Shuffle
                    )
                }
                else -> {
                    item(key = "view") {
                        TriTabs(
                            items = listOf(stringResource(Res.string.teams_view_pitch), stringResource(Res.string.teams_view_list)),
                            selectedIndex = if (state.view == TeamsView.PITCH) 0 else 1,
                            onSelect = { onAction(TeamsAction.OnViewSelected(if (it == 0) TeamsView.PITCH else TeamsView.LIST)) }
                        )
                    }
                    if (state.view == TeamsView.PITCH) {
                        // Equipos B (screen 06-campo): the pitch, then the balance
                        item(key = "pitch") { Pitch(teamA = teamA.map { it.player }, teamB = teamB.map { it.player }) }
                        state.balance?.let { balance ->
                            item(key = "balance") {
                                TeamBalance(
                                    ratingA = balance.teamA.averageRating,
                                    ratingB = balance.teamB.averageRating,
                                    expectedA = balance.teamAExpectedScore.toFloat(),
                                    modifier = Modifier.padding(top = Space.s3)
                                )
                            }
                        }
                    } else {
                        // Equipos A (screen 05-equipos): the notice, the balance card and both lists
                        item(key = "published") {
                            TriNotice(
                                title = stringResource(Res.string.teams_published_title),
                                body = myTeam?.let { stringResource(Res.string.teams_you_play_in, teamName(it.toTri())) },
                                tone = TriNoticeTone.Ok
                            )
                        }
                        state.balance?.let { balance ->
                            item(key = "balance-card") {
                                TriCard(modifier = Modifier.fillMaxWidth(), contentPadding = Space.s5) {
                                    TeamBalance(
                                        ratingA = balance.teamA.averageRating,
                                        ratingB = balance.teamB.averageRating,
                                        expectedA = balance.teamAExpectedScore.toFloat()
                                    )
                                }
                            }
                        }
                        item(key = "lists") {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                TeamList(TriTeam.A, teamA, state.myMemberId, Modifier.weight(1f))
                                TeamList(TriTeam.B, teamB, state.myMemberId, Modifier.weight(1f))
                            }
                        }
                    }
                    match?.let { m ->
                        item(key = "open-match") {
                            TriTextButton(text = stringResource(Res.string.teams_open_match), onClick = { onOpenMatch(m.id) }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamList(team: TriTeam, players: List<TeamPlayer>, myMemberId: String?, modifier: Modifier = Modifier) {
    val colors = Triangulacion.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TeamBadge(team, size = 18.dp)
            TriCaps(
                text = stringResource(if (team == TriTeam.A) DesignRes.string.tri_team_a_caps else DesignRes.string.tri_team_b_caps),
                color = if (team == TriTeam.A) colors.teamAText else colors.teamBText
            )
        }
        players.forEach { player ->
            TriCard(modifier = Modifier.fillMaxWidth(), highlighted = player.id == myMemberId, contentPadding = 0.dp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    TriAvatar(name = player.name, size = 30.dp, team = team)
                    Text(text = player.name, style = Triangulacion.type.label.copy(fontSize = Triangulacion.type.body.fontSize), color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    player.number?.let { Text(text = "#$it", style = Triangulacion.type.label.tabular, color = colors.inkMuted) }
                }
            }
        }
    }
}

