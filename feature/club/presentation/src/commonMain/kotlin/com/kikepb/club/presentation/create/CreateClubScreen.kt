package com.kikepb.club.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.kikepb.core.presentation.mediapicker.rememberImagePickerLauncher
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.textfields.SquadfyMultiLineTextField
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateClubRoot(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: CreateClubViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val imagePickerLauncher = rememberImagePickerLauncher { pickedImageData ->
        viewModel.onAction(
            CreateClubAction.OnLogoSelected(
                bytes = pickedImageData.bytes,
                mimeType = pickedImageData.mimeType
            )
        )
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            CreateClubEvent.Success -> onSuccess()
            is CreateClubEvent.Error -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asStringAsync())
            }
        }
    }

    CreateClubScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState,
        onPickLogo = { imagePickerLauncher.launch() }
    )
}

@Composable
fun CreateClubScreen(
    state: CreateClubState,
    onAction: (CreateClubAction) -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onPickLogo: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { SquadfyTopBar(title = "Crear un club", onBackClick = onBackClick) },
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
                    text = "Crea tu club",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
                Text(
                    text = "Define los datos de tu equipo. Solo el nombre es obligatorio.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.extended.textPlaceholder
                )
            }

            ClubLogoPickerCard(
                selectedBytes = state.logoSelection?.bytes,
                onPickLogo = onPickLogo,
                onClearLogo = { onAction(CreateClubAction.OnClearLogoSelection) },
                modifier = Modifier.fillMaxWidth()
            )

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
                        state = state.nameState,
                        modifier = Modifier.fillMaxWidth(),
                        title = "Nombre del club *",
                        placeholder = "Ej: Los Cracks FC",
                        singleLine = true,
                        isError = state.nameError != null,
                        supportingText = state.nameError?.asString() ?: "Máx. 120 caracteres",
                        keyboardType = KeyboardType.Text
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.extended.textSecondary
                        )
                        SquadfyMultiLineTextField(
                            state = state.descriptionState,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "Describe tu club... (opcional)",
                            maxHeightInLines = 4
                        )
                        Text(
                            text = "Opcional · Máx. 2000 caracteres",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.extended.textPlaceholder
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)

                    SquadfyTextField(
                        state = state.maxMembersState,
                        modifier = Modifier.fillMaxWidth(),
                        title = "Límite de miembros",
                        placeholder = "Ej: 20",
                        singleLine = true,
                        isError = state.maxMembersError != null,
                        supportingText = state.maxMembersError?.asString() ?: "Opcional · Sin límite si se deja vacío",
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            SquadfyButton(
                text = "Crear club",
                onClick = { onAction(CreateClubAction.OnCreateClub) },
                enabled = state.canSubmit,
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ClubLogoPickerCard(
    selectedBytes: ByteArray?,
    onPickLogo: () -> Unit,
    onClearLogo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)

    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable(onClick = onPickLogo),
                contentAlignment = Alignment.Center
            ) {
                if (selectedBytes != null) {
                    AsyncImage(
                        model = selectedBytes,
                        contentDescription = "Logo seleccionado",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Logo del club",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
                Text(
                    text = if (selectedBytes != null) "Imagen seleccionada · Toca para cambiar"
                    else "Opcional · Toca la imagen para elegir",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.extended.textPlaceholder
                )
            }

            if (selectedBytes != null) {
                IconButton(
                    onClick = onClearLogo,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Eliminar imagen",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.extended.textPlaceholder
                    )
                }
            }
        }
    }
}
