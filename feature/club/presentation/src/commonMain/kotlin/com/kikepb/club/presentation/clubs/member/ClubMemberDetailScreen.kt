package com.kikepb.club.presentation.clubs.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.core.designsystem.components.avatar.AvatarSize
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.components.layouts.SquadfySnackbarScaffold
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ClubMemberDetailRoot(
    onBackClick: () -> Unit,
    viewModel: ClubMemberDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is ClubMemberDetailEvent.ShowMessage -> {
                scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
            }
        }
    }

    ClubMemberDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun ClubMemberDetailScreen(
    state: ClubMemberDetailState,
    onAction: (ClubMemberDetailAction) -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val member = state.member

    SquadfySnackbarScaffold(
        snackbarHostState = snackbarHostState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.isLoading && member == null) {
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
                SquadfyButton(
                    text = "Volver",
                    style = SquadfyButtonStyle.SECONDARY,
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            member?.let { loadedMember ->
                item { MemberHeroHeader(member = loadedMember) }
                item { MemberIdentityCard(member = loadedMember) }
                item { MemberStatsCard(member = loadedMember) }
            } ?: item {
                Text(
                    text = "No hay información disponible de este integrante.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                SquadfyButton(
                    text = "Reintentar",
                    onClick = { onAction(ClubMemberDetailAction.OnRefresh) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MemberHeroHeader(member: ClubMemberModel) {
    val initials = remember(member.username) { initialsOf(member.username) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF111827), Color(0xFF1D4ED8), Color(0xFF06B6D4))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SquadfyAvatarPhoto(
                displayText = initials,
                size = AvatarSize.LARGE
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = member.username,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = member.position ?: "Sin posición definida",
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MemberIdentityCard(member: ClubMemberModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IdentityLine("Email", member.email)
            IdentityLine("Rol", member.role)
            IdentityLine("Dorsal", member.shirtNumber?.toString() ?: "-")
            IdentityLine("Posición", member.position ?: "-")
        }
    }
}

@Composable
private fun MemberStatsCard(member: ClubMemberModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Estadísticas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            IdentityLine("Partidos jugados", member.matchesPlayed.toString())
            IdentityLine("Minutos jugados", member.minutesPlayed.toString())
            IdentityLine("Goles", member.goalsScored.toString())
            IdentityLine("Asistencias", member.assists.toString())
            IdentityLine("Tarjetas amarillas", member.yellowCards.toString())
            IdentityLine("Tarjetas rojas", member.redCards.toString())
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

private fun initialsOf(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString(separator = "") { it.first().uppercase() }
        .ifBlank { "JG" }
