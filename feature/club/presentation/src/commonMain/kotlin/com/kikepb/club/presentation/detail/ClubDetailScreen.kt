package com.kikepb.club.presentation.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.presentation.detail.components.SquadfyClubDetailBanner
import com.kikepb.club.presentation.detail.components.SquadfyClubDetailClassificationTab
import com.kikepb.club.presentation.detail.components.SquadfyClubDetailMembersTab
import com.kikepb.club.presentation.settings.ClubSettingsTab
import org.jetbrains.compose.resources.stringResource
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.club_detail_load_error
import squadfy_app.feature.club.presentation.generated.resources.club_detail_load_error_hint
import squadfy_app.feature.club.presentation.generated.resources.club_detail_loading
import squadfy_app.feature.club.presentation.generated.resources.club_detail_title
import squadfy_app.feature.club.presentation.generated.resources.common_retry
import com.kikepb.club.presentation.detail.components.SquadfyClubDetailTabRow
import com.kikepb.club.presentation.match.MatchRoot
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
    onOpenBans: (clubId: String) -> Unit,
    onOpenSchedule: (clubId: String) -> Unit,
    onLeftClub: () -> Unit,
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
        onOpenBans = onOpenBans,
        onOpenSchedule = onOpenSchedule,
        onLeftClub = onLeftClub,
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
    onOpenBans: (clubId: String) -> Unit,
    onOpenSchedule: (clubId: String) -> Unit,
    onLeftClub: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val club = state.club
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        topBar = { SquadfyTopBar(title = club?.name ?: stringResource(Res.string.club_detail_title), onBackClick = onBackClick) },
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
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                onMemberClick = { memberId -> onMemberClick(club.id, memberId) },
                onOpenBans = { onOpenBans(club.id) },
                onOpenSchedule = { onOpenSchedule(club.id) },
                onLeftClub = onLeftClub,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ClubContent(
    club: ClubModel,
    state: ClubDetailState,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onMemberClick: (String) -> Unit,
    onOpenBans: () -> Unit,
    onOpenSchedule: () -> Unit,
    onLeftClub: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        SquadfyClubDetailBanner(
            club = club,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
        SquadfyClubDetailTabRow(selectedIndex = selectedTabIndex, onTabSelected = onTabSelected)
        AnimatedContent(
            targetState = selectedTabIndex,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
            modifier = Modifier.weight(1f)
        ) { tabIndex ->
            when (tabIndex) {
                0 -> SquadfyClubDetailClassificationTab(club = club, members = state.members, onMemberClick = onMemberClick)
                1 -> SquadfyClubDetailMembersTab(members = state.members, onMemberClick = onMemberClick)
                2 -> MatchRoot(snackbarHostState = snackbarHostState)
                3 -> ClubSettingsTab(
                    club = club,
                    myMembership = state.myMembership,
                    snackbarHostState = snackbarHostState,
                    onOpenBans = onOpenBans,
                    onOpenSchedule = onOpenSchedule,
                    onLeftClub = onLeftClub
                )
            }
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
                text = stringResource(Res.string.club_detail_loading),
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
                text = stringResource(Res.string.club_detail_load_error),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.extended.textSecondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(Res.string.club_detail_load_error_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.extended.textPlaceholder,
                textAlign = TextAlign.Center
            )
            SquadfyButton(text = stringResource(Res.string.common_retry), onClick = onRetry)
        }
    }
}
