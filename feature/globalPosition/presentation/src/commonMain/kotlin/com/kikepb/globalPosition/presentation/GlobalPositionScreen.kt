package com.kikepb.globalPosition.presentation

import com.kikepb.core.designsystem.components.loading.SquadfyLoadingIndicator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import squadfy_app.feature.globalposition.presentation.generated.resources.home_enroll_failed
import squadfy_app.feature.globalposition.presentation.generated.resources.home_no_clubs
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnSettingsClick
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.CopyToClipboard
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToClub
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToSettings
import com.kikepb.globalPosition.presentation.components.HomeClubCard
import com.kikepb.globalPosition.presentation.components.SectionHeader
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.globalposition.presentation.generated.resources.squadfy_global_position_home
import squadfy_app.feature.globalposition.presentation.generated.resources.squadfy_global_position_my_clubs
import squadfy_app.feature.globalposition.presentation.generated.resources.Res.string as RString

@Composable
fun GlobalPositionRoot(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToClub: (String) -> Unit = {},
    viewModel: GlobalPositionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is CopyToClipboard -> { clipboard.setText(AnnotatedString(text = event.code)) }
            NavigateToSettings -> onNavigateToSettings()
            is NavigateToClub -> onNavigateToClub(event.clubId)
            GlobalPositionEvent.EnrollFailed -> scope.launch { snackbarHostState.showSnackbar(getString(RString.home_enroll_failed)) }
        }
    }
    // AC-010-07: refresh when coming back to the foreground
    LifecycleResumeEffect(Unit) {
        viewModel.onAction(GlobalPositionAction.OnResume)
        onPauseOrDispose { }
    }

    GlobalPositionScreen(state = state, onAction = viewModel::onAction, snackbarHostState = snackbarHostState, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalPositionScreen(
    state: GlobalPositionUiState,
    onAction: (GlobalPositionAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { SquadfyTopBar(title = stringResource(RString.squadfy_global_position_home), onSettingsClick = { onAction(OnSettingsClick) }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = { onAction(GlobalPositionAction.OnRefresh) }) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 20.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "header_clubs") {
                SectionHeader(title = stringResource(RString.squadfy_global_position_my_clubs), modifier = Modifier.fillMaxWidth())
            }
            if (state.isLoadingClubs) item(key = "loading_clubs") { SquadfyLoadingIndicator() }
            if (!state.isLoadingClubs && state.cards.isEmpty()) {
                item(key = "no_clubs") {
                    Text(
                        text = stringResource(RString.home_no_clubs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.extended.textPlaceholder
                    )
                }
            }
            items(
                items = state.cards,
                key = { "club_${it.club.id}" }
            ) { card ->
                HomeClubCard(
                    card = card,
                    now = state.now,
                    onClick = { onAction(GlobalPositionAction.OnClubClick(card.club.id)) },
                    onEnroll = { onAction(GlobalPositionAction.OnEnrollClick(card.club.id)) },
                    onRetry = { onAction(GlobalPositionAction.OnRetryClub(card.club.id)) }
                )
            }
        }
        }
    }
}
