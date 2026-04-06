package com.kikepb.club.presentation.join

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun JoinClubRoot(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: JoinClubViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            JoinClubEvent.Success -> onSuccess()
            is JoinClubEvent.Error -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asStringAsync())
            }
        }
    }

    JoinClubScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun JoinClubScreen(
    state: JoinClubState,
    onAction: (JoinClubAction) -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { SquadfyTopBar(title = "Unirme a un club", onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Introduce el código",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
                Text(
                    text = "Pídele el código de invitación al administrador del club.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.extended.textPlaceholder
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SquadfyTextField(
                        state = state.invitationCodeState,
                        modifier = Modifier.fillMaxWidth(),
                        title = "Código de invitación *",
                        placeholder = "Ej: ABC123",
                        singleLine = true,
                        isError = state.invitationCodeError != null,
                        supportingText = state.invitationCodeError ?: "6–12 caracteres alfanuméricos",
                        keyboardType = KeyboardType.Text
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)

                    SquadfyTextField(
                        state = state.shirtNumberState,
                        modifier = Modifier.fillMaxWidth(),
                        title = "Número de camiseta",
                        placeholder = "Ej: 10",
                        singleLine = true,
                        isError = state.shirtNumberError != null,
                        supportingText = state.shirtNumberError ?: "Opcional · Entre 1 y 999",
                        keyboardType = KeyboardType.Number
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)

                    SquadfyTextField(
                        state = state.positionState,
                        modifier = Modifier.fillMaxWidth(),
                        title = "Posición",
                        placeholder = "Ej: Delantero centro",
                        singleLine = true,
                        supportingText = "Opcional · Máx. 120 caracteres",
                        keyboardType = KeyboardType.Text
                    )
                }
            }

            SquadfyButton(
                text = "Unirme al club",
                onClick = { onAction(JoinClubAction.OnSubmit) },
                enabled = state.canSubmit,
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
