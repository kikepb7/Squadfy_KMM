package com.kikepb.club.presentation.clubs.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.layouts.SquadfySnackbarScaffold
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ClubInfoCenterRoot(
    onNavigateToTeams: () -> Unit,
    viewModel: ClubInfoCenterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is ClubInfoCenterEvent.ShowMessage -> {
                scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
            }
            ClubInfoCenterEvent.NavigateToTeams -> onNavigateToTeams()
        }
    }

    ClubInfoCenterScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun ClubInfoCenterScreen(
    state: ClubInfoCenterState,
    onAction: (ClubInfoCenterAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    SquadfySnackbarScaffold(
        snackbarHostState = snackbarHostState,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Centro de información",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Desde aquí puedes crear un equipo o unirte con invitación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Crear equipo", style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = state.newClubName,
                            onValueChange = { onAction(ClubInfoCenterAction.OnClubNameChange(it)) },
                            label = { Text("Nombre del equipo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.newClubDescription,
                            onValueChange = { onAction(ClubInfoCenterAction.OnClubDescriptionChange(it)) },
                            label = { Text("Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.newClubMaxMembers,
                                onValueChange = { onAction(ClubInfoCenterAction.OnClubMaxMembersChange(it)) },
                                label = { Text("Máx. miembros") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = state.newClubLogoUrl,
                                onValueChange = { onAction(ClubInfoCenterAction.OnClubLogoUrlChange(it)) },
                                label = { Text("Logo URL") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        SquadfyButton(
                            text = "Crear equipo",
                            onClick = { onAction(ClubInfoCenterAction.OnCreateClubClick) },
                            enabled = !state.isSubmitting,
                            isLoading = state.isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Unirme a un equipo", style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = state.invitationCode,
                            onValueChange = { onAction(ClubInfoCenterAction.OnInvitationCodeChange(it)) },
                            label = { Text("Código de invitación") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.shirtNumber,
                                onValueChange = { onAction(ClubInfoCenterAction.OnShirtNumberChange(it)) },
                                label = { Text("Dorsal") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = state.position,
                                onValueChange = { onAction(ClubInfoCenterAction.OnPositionChange(it)) },
                                label = { Text("Posición") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        SquadfyButton(
                            text = "Unirme",
                            onClick = { onAction(ClubInfoCenterAction.OnJoinClubClick) },
                            enabled = !state.isSubmitting,
                            isLoading = state.isSubmitting,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Cuando crees o te unas a un equipo, volverás automáticamente a la lista de equipos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
