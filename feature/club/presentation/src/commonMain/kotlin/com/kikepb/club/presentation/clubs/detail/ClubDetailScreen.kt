package com.kikepb.club.presentation.clubs.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.core.designsystem.components.avatar.AvatarSize
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.components.layouts.SquadfySnackbarScaffold
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ClubDetailRoot(
    onBackClick: () -> Unit,
    onMemberClick: (clubId: String, memberId: String) -> Unit,
    viewModel: ClubDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is ClubDetailEvent.ShowMessage -> {
                scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
            }
        }
    }

    ClubDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        onMemberClick = onMemberClick,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun ClubDetailScreen(
    state: ClubDetailState,
    onAction: (ClubDetailAction) -> Unit,
    onBackClick: () -> Unit,
    onMemberClick: (clubId: String, memberId: String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val club = state.club

    SquadfySnackbarScaffold(
        snackbarHostState = snackbarHostState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.isLoading && club == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
            return@SquadfySnackbarScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SquadfyButton(
                        text = "Volver",
                        style = SquadfyButtonStyle.SECONDARY,
                        onClick = onBackClick
                    )
                    SquadfyButton(
                        text = "Actualizar",
                        onClick = { onAction(ClubDetailAction.OnRefresh) },
                        enabled = !state.isLoading
                    )
                }
            }

            club?.let { loadedClub ->
                item { ClubHeroHeader(club = loadedClub) }
                item { ClubIdentityCard(club = loadedClub) }

                item {
                    Text(
                        text = "Clasificación",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tabla tipo liga con rendimiento individual de la plantilla.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    if (state.members.isEmpty()) {
                        Text(
                            text = "Este equipo todavía no tiene jugadores.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        StandingsTable(
                            members = state.members,
                            onMemberClick = { memberId -> onMemberClick(loadedClub.id, memberId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClubHeroHeader(club: ClubModel) {
    val initials = remember(club.name) { initialsOf(club.name) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E40AF), Color(0xFF0EA5E9))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SquadfyAvatarPhoto(
                displayText = initials,
                imageUrl = club.clubLogoUrl,
                size = AvatarSize.LARGE
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = club.name,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ClubIdentityCard(club: ClubModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!club.description.isNullOrBlank()) {
                Text(
                    text = club.description.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IdentityLine(label = "Código", value = club.invitationCode)
            IdentityLine(label = "Plantilla", value = "${club.membersCount}${club.maxMembers?.let { " / $it" } ?: ""}")
            IdentityLine(label = "Temporada", value = "2026")
        }
    }
}

@Composable
private fun IdentityLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StandingsTable(
    members: List<ClubMemberModel>,
    onMemberClick: (String) -> Unit
) {
    val standings = remember(members) {
        members
            .map { it.toStandingRow() }
            .sortedWith(compareByDescending<StandingRow> { it.points }.thenByDescending { it.rating })
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        val horizontalScroll = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .horizontalScroll(horizontalScroll)
        ) {
            StandingsHeaderRow()
            standings.forEachIndexed { index, row ->
                StandingsValueRow(
                    index = index,
                    row = row,
                    onClick = { onMemberClick(row.memberId) }
                )
            }
        }
    }
}

@Composable
private fun StandingsHeaderRow() {
    val headers = listOf("N", "Jugador", "VAL", "PJ", "PG", "PE", "PP", "G", "MIN", "TA", "TR", "PTS")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        headers.forEach { title ->
            TableCell(text = title, isHeader = true)
        }
    }
}

@Composable
private fun StandingsValueRow(
    index: Int,
    row: StandingRow,
    onClick: () -> Unit
) {
    val rowColor = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(text = row.shirtNumber)
        TableCell(text = row.playerName)
        TableCell(text = row.rating)
        TableCell(text = row.played.toString())
        TableCell(text = row.wins.toString())
        TableCell(text = row.draws.toString())
        TableCell(text = row.losses.toString())
        TableCell(text = row.goals.toString())
        TableCell(text = row.minutes.toString())
        TableCell(text = row.yellow.toString())
        TableCell(text = row.red.toString())
        TableCell(text = row.points.toString())
    }
}

@Composable
private fun TableCell(
    text: String,
    isHeader: Boolean = false,
    alignStart: Boolean = false
) {
    val width = if (alignStart) 150.dp else 58.dp

    Text(
        text = text,
        style = if (isHeader) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodySmall,
        fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = if (alignStart) TextAlign.Start else TextAlign.Center,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 2.dp)
    )
}

private data class StandingRow(
    val memberId: String,
    val shirtNumber: String,
    val playerName: String,
    val rating: String,
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goals: Int,
    val minutes: Int,
    val yellow: Int,
    val red: Int,
    val points: Int
)

private fun ClubMemberModel.toStandingRow(): StandingRow {
    val wins = minOf(matchesPlayed, goalsScored)
    val remaining = (matchesPlayed - wins).coerceAtLeast(0)
    val draws = minOf(remaining, assists)
    val losses = (matchesPlayed - wins - draws).coerceAtLeast(0)

    val rawRating = if (matchesPlayed == 0) 0.0 else {
        ((goalsScored * 4.0) + (assists * 3.0) + (minutesPlayed / 45.0) - (yellowCards * 0.5) - (redCards * 1.0)) /
            matchesPlayed
    }
    val ratingRounded = ((rawRating * 10.0).toInt() / 10.0)

    return StandingRow(
        memberId = id,
        shirtNumber = shirtNumber?.toString() ?: "-",
        playerName = username,
        rating = ratingRounded.toString(),
        played = matchesPlayed,
        wins = wins,
        draws = draws,
        losses = losses,
        goals = goalsScored,
        minutes = minutesPlayed,
        yellow = yellowCards,
        red = redCards,
        points = wins * 3 + draws
    )
}

private fun initialsOf(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString(separator = "") { it.first().uppercase() }
        .ifBlank { "CL" }
