package com.kikepb.club.presentation.match

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.ClubMatchModel
import com.kikepb.club.domain.model.MatchParticipantModel
import com.kikepb.club.presentation.detail.components.EmptyTabMessage
import com.kikepb.club.presentation.detail.components.SquadfyClubDetailTabSectionTitle
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private val POSITION_OPTIONS = listOf(
    "GOALKEEPER" to "Portero",
    "DEFENDER" to "Defensa",
    "MIDFIELDER" to "Centrocampista",
    "FORWARD" to "Delantero"
)

private fun positionLabel(position: String?): String =
    POSITION_OPTIONS.find { it.first == position }?.second ?: "Sin posición"

@Composable
fun MatchRoot(
    snackbarHostState: SnackbarHostState,
    viewModel: MatchViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is MatchEvent.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asStringAsync())
            }
        }
    }

    MatchScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun MatchScreen(state: MatchState, onAction: (MatchAction) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SquadfyClubDetailTabSectionTitle("Partido") }

        when {
            state.match == null -> item {
                NoMatchContent(isAdmin = state.isAdmin && state.isTestMatchEnabled, isLoading = state.isPerformingAction, onCreateMatch = { onAction(MatchAction.OnCreateTestMatch) })
            }
            else -> {
                val match = state.match
                item { MatchInfoCard(match = match) }

                when (match.status) {
                    "SCHEDULED" -> {
                        item {
                            SignupActionsRow(
                                state = state,
                                onAction = onAction
                            )
                        }
                        item { SquadfyClubDetailTabSectionTitle("Apuntados (${state.confirmedSignups.size})") }
                        if (state.confirmedSignups.isEmpty()) {
                            item { EmptyTabMessage("Todavía no se ha apuntado nadie.") }
                        } else {
                            items(state.confirmedSignups) { signup ->
                                val member = signup.clubMemberId?.let { id -> state.members.find { it.id == id } }
                                SignupRow(
                                    name = signup.guestName ?: member?.username ?: "Jugador",
                                    position = signup.position ?: member?.position,
                                    rating = signup.rating ?: member?.rating,
                                    isGuest = signup.clubMemberId == null,
                                    canRemove = state.isAdmin || signup.clubMemberId == state.myMemberId,
                                    onRemove = { onAction(MatchAction.OnRemoveSignup(signup.id)) }
                                )
                            }
                        }
                    }
                    "TEAMS_GENERATED" -> {
                        item { TeamSection(title = "Equipo A", participants = match.teamA, state = state, onAction = onAction) }
                        item { TeamSection(title = "Equipo B", participants = match.teamB, state = state, onAction = onAction) }
                        if (state.isAdmin && state.isManualScoreEnabled) {
                            item {
                                SquadfyButton(
                                    text = "Registrar resultado",
                                    onClick = { onAction(MatchAction.OnShowResultDialog) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    "COMPLETED" -> {
                        item { CompletedResultCard(teamAScore = match.teamAScore, teamBScore = match.teamBScore) }
                        item { TeamSection(title = "Equipo A", participants = match.teamA, state = state, onAction = onAction, readOnly = true) }
                        item { TeamSection(title = "Equipo B", participants = match.teamB, state = state, onAction = onAction, readOnly = true) }
                    }
                    "CANCELLED" -> item { EmptyTabMessage("Este partido se ha cancelado.") }
                }
            }
        }
    }

    if (state.showAddGuestDialog && state.isGuestsEnabled) {
        AddGuestDialog(state = state, onAction = onAction)
    }
    if (state.showResultDialog && state.isManualScoreEnabled) {
        ResultDialog(state = state, onAction = onAction)
    }
}

@Composable
private fun NoMatchContent(isAdmin: Boolean, isLoading: Boolean, onCreateMatch: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No hay ningún partido programado todavía.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textPlaceholder
            )
            if (isAdmin) {
                SquadfyButton(
                    text = "Crear partido de prueba",
                    onClick = onCreateMatch,
                    isLoading = isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MatchInfoCard(match: ClubMatchModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoRow(label = "Fecha", value = match.scheduledAt)
            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            InfoRow(label = "Estado", value = statusLabel(match.status))
            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            InfoRow(label = "Inscripciones abren", value = match.signupOpensAt)
            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            InfoRow(label = "Inscripciones cierran", value = match.signupClosesAt)
        }
    }
}

private fun statusLabel(status: String): String = when (status) {
    "SCHEDULED" -> "Abierto para apuntarse"
    "TEAMS_GENERATED" -> "Equipos sorteados"
    "COMPLETED" -> "Finalizado"
    "CANCELLED" -> "Cancelado"
    else -> status
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textPlaceholder)
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
    }
}

@Composable
private fun SignupActionsRow(state: MatchState, onAction: (MatchAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.mySignup == null) {
            SquadfyButton(
                text = "Apuntarme",
                onClick = { onAction(MatchAction.OnSignUp) },
                isLoading = state.isPerformingAction,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            SquadfyButton(
                text = "Cancelar mi apunte",
                onClick = { onAction(MatchAction.OnCancelSignup) },
                style = SquadfyButtonStyle.DESTRUCTIVE_SECONDARY,
                isLoading = state.isPerformingAction,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (state.isAdmin) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (state.isGuestsEnabled) {
                    SquadfyButton(
                        text = "Añadir invitado",
                        onClick = { onAction(MatchAction.OnShowAddGuestDialog) },
                        style = SquadfyButtonStyle.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )
                }
                SquadfyButton(
                    text = "Sortear equipos",
                    onClick = { onAction(MatchAction.OnGenerateTeams) },
                    enabled = state.confirmedSignups.size >= 2,
                    isLoading = state.isPerformingAction,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SignupRow(
    name: String,
    position: String?,
    rating: Int?,
    isGuest: Boolean,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isGuest) "$name (invitado)" else name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
                Text(
                    text = "${positionLabel(position)}${rating?.let { " · $it" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.extended.textPlaceholder
                )
            }
            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Quitar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamSection(
    title: String,
    participants: List<MatchParticipantModel>,
    state: MatchState,
    onAction: (MatchAction) -> Unit,
    readOnly: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SquadfyClubDetailTabSectionTitle(title)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column {
                participants.forEachIndexed { index, participant ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
                    ParticipantStatRow(
                        participant = participant,
                        draft = participant.clubMemberId?.let { state.playerStats[it] },
                        canEditStats = state.isAdmin && !readOnly && participant.clubMemberId != null,
                        onIncrement = { statType -> participant.clubMemberId?.let { onAction(MatchAction.OnIncrementStat(it, statType)) } }
                    )
                }
                if (participants.isEmpty()) {
                    EmptyTabMessage("Sin jugadores asignados.")
                }
            }
        }
    }
}

@Composable
private fun ParticipantStatRow(
    participant: MatchParticipantModel,
    draft: PlayerStatDraft?,
    canEditStats: Boolean,
    onIncrement: (StatType) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(
            text = participant.displayName,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
        Text(
            text = "${positionLabel(participant.position)} · ${participant.rating}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.extended.textPlaceholder
        )
        if (canEditStats) {
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCounter(label = "G", count = draft?.goals ?: 0, onAdd = { onIncrement(StatType.GOAL) })
                StatCounter(label = "A", count = draft?.assists ?: 0, onAdd = { onIncrement(StatType.ASSIST) })
                StatCounter(label = "TA", count = draft?.yellowCards ?: 0, onAdd = { onIncrement(StatType.YELLOW) })
                StatCounter(label = "TR", count = draft?.redCards ?: 0, onAdd = { onIncrement(StatType.RED) })
            }
        }
    }
}

@Composable
private fun StatCounter(label: String, count: Int, onAdd: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .clickable(onClick = onAdd)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = "$label $count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = "Sumar $label",
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun CompletedResultCard(teamAScore: Int?, teamBScore: Int?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${teamAScore ?: "-"}  -  ${teamBScore ?: "-"}",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AddGuestDialog(state: MatchState, onAction: (MatchAction) -> Unit) {
    Dialog(onDismissRequest = { onAction(MatchAction.OnDismissAddGuestDialog) }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Añadir invitado",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
                SquadfyTextField(
                    state = state.guestNameState,
                    modifier = Modifier.fillMaxWidth(),
                    title = "Nombre *",
                    placeholder = "Ej: Juan Pérez",
                    singleLine = true
                )
                Text(
                    text = "Posición",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.extended.textPlaceholder
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    POSITION_OPTIONS.forEach { (value, label) ->
                        PositionChip(
                            label = label,
                            selected = state.guestPosition == value,
                            onClick = { onAction(MatchAction.OnGuestPositionSelected(if (state.guestPosition == value) null else value)) }
                        )
                    }
                }
                SquadfyTextField(
                    state = state.guestRatingState,
                    modifier = Modifier.fillMaxWidth(),
                    title = "Puntuación (1-99)",
                    placeholder = "Ej: 50",
                    singleLine = true,
                    keyboardType = KeyboardType.Number,
                    supportingText = "Opcional · Se usa para equilibrar los equipos"
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SquadfyButton(text = "Cancelar", onClick = { onAction(MatchAction.OnDismissAddGuestDialog) }, style = SquadfyButtonStyle.SECONDARY)
                    SquadfyButton(
                        text = "Añadir",
                        onClick = { onAction(MatchAction.OnConfirmAddGuest) },
                        isLoading = state.isPerformingAction,
                        enabled = state.guestNameState.text.isNotBlank()
                    )
                }
            }
        }
    }
}

@Composable
private fun PositionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ResultDialog(state: MatchState, onAction: (MatchAction) -> Unit) {
    Dialog(onDismissRequest = { onAction(MatchAction.OnDismissResultDialog) }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Registrar resultado",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
                Text(
                    text = "Los goles/asistencias/tarjetas se suman tocando el botón junto a cada jugador en las listas de equipos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.extended.textPlaceholder
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SquadfyTextField(
                        state = state.teamAScoreState,
                        modifier = Modifier.weight(1f),
                        title = "Goles equipo A",
                        placeholder = "0",
                        singleLine = true,
                        keyboardType = KeyboardType.Number
                    )
                    SquadfyTextField(
                        state = state.teamBScoreState,
                        modifier = Modifier.weight(1f),
                        title = "Goles equipo B",
                        placeholder = "0",
                        singleLine = true,
                        keyboardType = KeyboardType.Number
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SquadfyButton(text = "Cancelar", onClick = { onAction(MatchAction.OnDismissResultDialog) }, style = SquadfyButtonStyle.SECONDARY)
                    SquadfyButton(
                        text = "Guardar",
                        onClick = { onAction(MatchAction.OnConfirmResult) },
                        isLoading = state.isPerformingAction
                    )
                }
            }
        }
    }
}
