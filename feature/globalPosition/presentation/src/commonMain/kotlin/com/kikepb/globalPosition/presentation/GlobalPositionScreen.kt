package com.kikepb.globalPosition.presentation

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnSettingsClick
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.CopyToClipboard
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToClub
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToSettings
import com.kikepb.globalPosition.presentation.components.ClubCard
import com.kikepb.globalPosition.presentation.components.MatchCard
import com.kikepb.globalPosition.presentation.components.NewsCard
import com.kikepb.globalPosition.presentation.components.SectionHeader
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.globalposition.presentation.generated.resources.squadfy_global_position_home
import squadfy_app.feature.globalposition.presentation.generated.resources.squadfy_global_position_last_matches
import squadfy_app.feature.globalposition.presentation.generated.resources.squadfy_global_position_last_news
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

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is CopyToClipboard -> { clipboard.setText(AnnotatedString(text = event.code)) }
            NavigateToSettings -> onNavigateToSettings()
            is NavigateToClub -> onNavigateToClub(event.clubId)
        }
    }

    GlobalPositionScreen(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
fun GlobalPositionScreen(
    state: GlobalPositionUiState,
    onAction: (GlobalPositionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { SquadfyTopBar(title = stringResource(RString.squadfy_global_position_home), onSettingsClick = { onAction(OnSettingsClick) }) }
    ) { innerPadding ->
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
            items(
                items = state.clubs,
                key = { "club_${it.id}" }
            ) { club ->
                ClubCard(
                    club = club,
                    onCopyInviteCode = { code ->
                        onAction(GlobalPositionAction.OnCopyInviteCode(code))
                    },
                    onClick = { onAction(GlobalPositionAction.OnClubClick(club.id)) }
                )
            }

            item(key = "spacer_matches") { Spacer(modifier = Modifier.height(8.dp)) }
            item(key = "header_matches") {
                SectionHeader(title = stringResource(RString.squadfy_global_position_last_matches), modifier = Modifier.fillMaxWidth())
            }
            items(
                items = state.matches,
                key = { "match_${it.id}" }
            ) { match ->
                MatchCard(match = match)
            }

            item(key = "spacer_news") { Spacer(modifier = Modifier.height(8.dp)) }
            item(key = "header_news") {
                SectionHeader(title = stringResource(RString.squadfy_global_position_last_news), modifier = Modifier.fillMaxWidth())
            }
            items(
                items = state.news,
                key = { "news_${it.id}" }
            ) { newsItem ->
                NewsCard(news = newsItem)
            }
        }
    }
}
