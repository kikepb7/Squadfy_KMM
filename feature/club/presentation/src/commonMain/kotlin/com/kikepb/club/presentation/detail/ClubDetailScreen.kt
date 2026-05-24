package com.kikepb.club.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.presentation.detail.components.SquadfyClubDetailBanner
import com.kikepb.club.presentation.detail.components.SquadfyClubDetailClassificationSection
import com.kikepb.club.presentation.detail.components.SquadfyClubDetailLastMatchSection
import com.kikepb.club.presentation.detail.components.SquadfyClubDetailTopScorersSection
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
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
            is ClubDetailEvent.ShowMessage ->
                scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubDetailScreen(
    state: ClubDetailState,
    onAction: (ClubDetailAction) -> Unit,
    onBackClick: () -> Unit,
    onMemberClick: (clubId: String, memberId: String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val club = state.club

    Scaffold(
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        topBar = { SquadfyTopBar(title = club?.name ?: "Club", onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading && club == null -> LoadingContent(Modifier.padding(padding))
            club == null -> {
                EmptyContent(
                    modifier = Modifier.padding(paddingValues = padding),
                    onRetry = { onAction(ClubDetailAction.OnRefresh) }
                )
            }
            else -> ClubContent(
                club = club,
                state = state,
                onMemberClick = { memberId -> onMemberClick(club.id, memberId) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ClubContent(
    club: ClubModel,
    state: ClubDetailState,
    onMemberClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item(key = "banner") {
            SquadfyClubDetailBanner(club = club)
        }

        item(key = "classification") {
            SquadfyClubDetailClassificationSection(
                members = state.members,
                onMemberClick = onMemberClick,
                onSeeAllClick = { /* TODO: navigate to full classification */ },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
            )
        }

        item(key = "last_match") {
            SquadfyClubDetailLastMatchSection(
                lastMatch = state.lastMatch,
                onSeeAllClick = { /* TODO: navigate to matches list */ },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp)
            )
        }

        item(key = "top_scorers") {
            SquadfyClubDetailTopScorersSection(
                members = state.members,
                onRankingClick = { /* TODO: navigate to full ranking */ },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
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
                text = "Cargando club...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textPlaceholder
            )
        }
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Text(
                text = "No se pudo cargar el club",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.extended.textSecondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Comprueba tu conexión e inténtalo de nuevo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.extended.textPlaceholder,
                textAlign = TextAlign.Center
            )
            SquadfyButton(text = "Reintentar", onClick = onRetry)
        }
    }
}
