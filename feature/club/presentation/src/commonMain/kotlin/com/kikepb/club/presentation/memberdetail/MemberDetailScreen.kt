package com.kikepb.club.presentation.memberdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.core.presentation.mediapicker.rememberImagePickerLauncher
import com.kikepb.club.presentation.memberdetail.MemberDetailAction.OnPhotoSelected
import com.kikepb.club.presentation.memberdetail.MemberDetailAction.OnUploadPhoto
import com.kikepb.club.presentation.memberdetail.MemberDetailEvent.PhotoUpdated
import com.kikepb.club.presentation.memberdetail.MemberDetailEvent.ShowMessage
import com.kikepb.club.presentation.memberdetail.components.MemberDisciplinarySection
import com.kikepb.club.presentation.memberdetail.components.MemberHeroCard
import com.kikepb.club.presentation.memberdetail.components.MemberInfoSection
import com.kikepb.club.presentation.memberdetail.components.MemberStatsSection
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MemberDetailRoot(
    onBackClick: () -> Unit,
    viewModel: MemberDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val imagePickerLauncher = rememberImagePickerLauncher { picked ->
        viewModel.onAction(OnPhotoSelected(bytes = picked.bytes, mimeType = picked.mimeType))
    }

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            PhotoUpdated -> scope.launch {
                snackbarHostState.showSnackbar(message = "Foto actualizada correctamente")
            }
            is ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(message = event.message.asStringAsync())
            }
        }
    }

    MemberDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        onPickPhoto = { imagePickerLauncher.launch() },
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun MemberDetailScreen(
    state: MemberDetailState,
    onAction: (MemberDetailAction) -> Unit,
    onBackClick: () -> Unit,
    onPickPhoto: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { SquadfyTopBar(title = "Perfil del jugador", onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading && state.member == null -> MemberLoadingContent(modifier = Modifier.padding(padding))
            state.member == null -> MemberNotFoundContent(modifier = Modifier.padding(padding))
            else -> MemberContent(
                member = state.member,
                state = state,
                onAction = onAction,
                onPickPhoto = onPickPhoto,
                contentPadding = padding
            )
        }
    }
}

@Composable
private fun MemberContent(
    member: ClubMemberModel,
    state: MemberDetailState,
    onAction: (MemberDetailAction) -> Unit,
    onPickPhoto: () -> Unit,
    contentPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MemberHeroCard(
            member = member,
            isOwnProfile = state.isOwnProfile,
            pendingPhotoBytes = state.pendingPhotoSelection?.bytes,
            isUploadingPhoto = state.isUploadingPhoto,
            onPickPhoto = onPickPhoto,
            onUploadPhoto = { onAction(OnUploadPhoto) }
        )
        MemberStatsSection(member = member, modifier = Modifier.fillMaxWidth())
        MemberDisciplinarySection(member = member, modifier = Modifier.fillMaxWidth())
        MemberInfoSection(member = member, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MemberLoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Text(
                text = "Cargando jugador...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textPlaceholder
            )
        }
    }
}

@Composable
private fun MemberNotFoundContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Text(
                text = "Jugador no encontrado",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.extended.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
