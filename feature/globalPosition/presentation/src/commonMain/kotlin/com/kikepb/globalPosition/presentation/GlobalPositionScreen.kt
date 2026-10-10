package com.kikepb.globalPosition.presentation

import kotlin.time.Clock
import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.core.designsystem.components.loading.SquadfyLoadingIndicator
import com.kikepb.core.designsystem.components.systembars.StatusBarIconsEffect
import com.kikepb.core.designsystem.triangulacion.components.TriAvatar
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriCaps
import com.kikepb.core.designsystem.triangulacion.components.TriSectionTitle
import com.kikepb.core.designsystem.triangulacion.format.TriDates
import com.kikepb.core.designsystem.triangulacion.format.formatLongDayCaps
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.presentation.util.ObserveAsEvents
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnSettingsClick
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.CopyToClipboard
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToClub
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToSettings
import com.kikepb.globalPosition.presentation.components.HomeClubRow
import com.kikepb.globalPosition.presentation.components.LastMatchdayCard
import com.kikepb.globalPosition.presentation.components.NextMatchBlock
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.globalposition.presentation.generated.resources.home_empty_action
import squadfy_app.feature.globalposition.presentation.generated.resources.home_empty_title
import squadfy_app.feature.globalposition.presentation.generated.resources.home_enroll_failed
import squadfy_app.feature.globalposition.presentation.generated.resources.home_greeting
import squadfy_app.feature.globalposition.presentation.generated.resources.home_greeting_anonymous
import squadfy_app.feature.globalposition.presentation.generated.resources.home_last_matchday
import squadfy_app.feature.globalposition.presentation.generated.resources.home_no_clubs
import squadfy_app.feature.globalposition.presentation.generated.resources.home_profile
import squadfy_app.feature.globalposition.presentation.generated.resources.home_your_clubs
import squadfy_app.core.designsystem.generated.resources.Res as DesignRes
import squadfy_app.core.designsystem.generated.resources.tri_see_all
import squadfy_app.feature.globalposition.presentation.generated.resources.Res.string as RString

@Composable
fun GlobalPositionRoot(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToClub: (String) -> Unit = {},
    onNavigateToClubs: () -> Unit = {},
    onNavigateToMatchResult: (clubId: String, matchId: String) -> Unit = { _, _ -> },
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
            GlobalPositionEvent.NavigateToClubs -> onNavigateToClubs()
            is GlobalPositionEvent.NavigateToMatchResult -> onNavigateToMatchResult(event.clubId, event.matchId)
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

/**
 * Home (spec 018, Inicio A): greeting, the ticket of the next match with the one action that matters, «Tus clubs»
 * and «Última jornada». Spec 017 loading semantics are unchanged.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalPositionScreen(
    state: GlobalPositionUiState,
    onAction: (GlobalPositionAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val colors = Triangulacion.colors
    val next = remember(state.cards, state.now) {
        state.cards.filter { it.current != null }.minByOrNull { it.current!!.matchScheduledAt }
    }
    val others = state.cards.filter { it.club.id != next?.club?.id }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.surface100,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            HomeHeader(
                userName = state.userName,
                today = TriDates.local(state.now, null).date,
                onProfileClick = { onAction(OnSettingsClick) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = { onAction(GlobalPositionAction.OnRefresh) }) {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + Space.s2,
                    bottom = innerPadding.calculateBottomPadding() + Space.s6,
                    start = Space.s4,
                    end = Space.s4
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (state.isLoadingClubs) item(key = "loading_clubs") { SquadfyLoadingIndicator() }
                if (!state.isLoadingClubs && state.cards.isEmpty()) {
                    item(key = "no_clubs") {
                        Column(verticalArrangement = Arrangement.spacedBy(Space.s3), modifier = Modifier.padding(top = Space.s6)) {
                            Text(text = stringResource(RString.home_empty_title), style = Triangulacion.type.displayMd, color = colors.ink)
                            Text(text = stringResource(RString.home_no_clubs), style = Triangulacion.type.body, color = colors.inkMuted)
                            TriButton(text = stringResource(RString.home_empty_action), onClick = { onAction(GlobalPositionAction.OnSeeAllClubsClick) }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
                if (next != null) {
                    item(key = "next_${next.club.id}") {
                        NextMatchBlock(
                            card = next,
                            now = state.now,
                            onOpenClub = { onAction(GlobalPositionAction.OnClubClick(next.club.id)) },
                            onEnroll = { onAction(GlobalPositionAction.OnEnrollClick(next.club.id)) }
                        )
                    }
                }
                if (others.isNotEmpty()) {
                    item(key = "header_clubs") {
                        TriSectionTitle(
                            text = stringResource(RString.home_your_clubs),
                            actionText = stringResource(DesignRes.string.tri_see_all),
                            onAction = { onAction(GlobalPositionAction.OnSeeAllClubsClick) },
                            modifier = Modifier.padding(top = Space.s3)
                        )
                    }
                    items(items = others, key = { "club_${it.club.id}" }) { card ->
                        HomeClubRow(
                            card = card,
                            now = state.now,
                            onClick = { onAction(GlobalPositionAction.OnClubClick(card.club.id)) },
                            onRetry = { onAction(GlobalPositionAction.OnRetryClub(card.club.id)) }
                        )
                    }
                }
                state.lastMatchday?.let { matchday ->
                    item(key = "header_last") {
                        TriSectionTitle(text = stringResource(RString.home_last_matchday), modifier = Modifier.padding(top = Space.s3))
                    }
                    item(key = "last_${matchday.matchId}") {
                        LastMatchdayCard(matchday = matchday, onClick = { onAction(GlobalPositionAction.OnLastMatchdayClick(matchday.clubId, matchday.matchId)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(userName: String?, today: kotlinx.datetime.LocalDate, onProfileClick: () -> Unit) {
    val colors = Triangulacion.colors
    StatusBarIconsEffect(background = colors.surface100)
    val profile = stringResource(RString.home_profile)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(start = Space.s4, end = Space.s4, top = Space.s3, bottom = Space.s3)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            TriCaps(text = formatLongDayCaps(today))
            Text(
                text = userName?.let { stringResource(RString.home_greeting, it) } ?: stringResource(RString.home_greeting_anonymous),
                style = Triangulacion.type.displayLg,
                color = colors.ink,
                maxLines = 1,
                modifier = Modifier.semantics { heading() }
            )
        }
        TriAvatar(
            name = userName ?: "?",
            size = 44.dp,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onProfileClick)
                .semantics { contentDescription = profile }
        )
    }
}

// Previews (spec 018): Tiza and Noche with the default state, no sample data

@Preview(name = "03 Inicio · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun HomeTizaPreview() = ScreenPreview { GlobalPositionScreen(state = GlobalPositionUiState(now = Clock.System.now()), onAction = {}) }

@Preview(name = "03 Inicio · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun HomeNochePreview() = ScreenPreview(darkTheme = true) { GlobalPositionScreen(state = GlobalPositionUiState(now = Clock.System.now()), onAction = {}) }

@Preview(name = "03b Inicio sin clubs · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun HomeEmptyTizaPreview() = ScreenPreview { GlobalPositionScreen(state = GlobalPositionUiState(now = Clock.System.now(), isLoadingClubs = false, hasFetchedClubs = true), onAction = {}) }

@Preview(name = "03b Inicio sin clubs · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun HomeEmptyNochePreview() = ScreenPreview(darkTheme = true) { GlobalPositionScreen(state = GlobalPositionUiState(now = Clock.System.now(), isLoadingClubs = false, hasFetchedClubs = true), onAction = {}) }
