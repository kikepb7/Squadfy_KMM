package com.kikepb.globalPosition.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import com.kikepb.globalPosition.presentation.GlobalPositionAction.OnToggleMatchParticipation
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.CopyToClipboard
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToClub
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToJoinClub
import com.kikepb.globalPosition.presentation.GlobalPositionEvent.NavigateToSettings
import com.kikepb.globalPosition.presentation.components.ClubCard
import com.kikepb.globalPosition.presentation.components.HomeHeader
import com.kikepb.globalPosition.presentation.components.JoinClubCard
import com.kikepb.globalPosition.presentation.components.MatchCard
import com.kikepb.globalPosition.presentation.components.NewsCard
import com.kikepb.globalPosition.presentation.components.NextMatchCard
import com.kikepb.globalPosition.presentation.components.SectionHeader
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GlobalPositionRoot(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToClub: (String) -> Unit = {},
    onNavigateToJoinClub: () -> Unit = {},
    viewModel: GlobalPositionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is CopyToClipboard -> clipboard.setText(AnnotatedString(text = event.code))
            NavigateToSettings -> onNavigateToSettings()
            is NavigateToClub -> onNavigateToClub(event.clubId)
            NavigateToJoinClub -> onNavigateToJoinClub()
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
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item(key = "header") {
                HomeHeader(
                    userName = state.userName,
                    currentDate = state.currentDate,
                    topPadding = innerPadding.calculateTopPadding(),
                    onNotificationsClick = { /* TODO: notifications */ },
                    onProfileClick = { onAction(GlobalPositionAction.OnSettingsClick) }
                )
            }

            item(key = "clubs_header") {
                SectionHeader(
                    title = "Mis Clubs",
                    actionLabel = "Ver todos",
                    onActionClick = { /* TODO: navigate to clubs list */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 24.dp, bottom = 12.dp)
                )
            }

            item(key = "clubs_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.clubs,
                        key = { "club_${it.id}" }
                    ) { club ->
                        ClubCard(
                            club = club,
                            onClick = { onAction(GlobalPositionAction.OnClubClick(club.id)) },
                            onCopyInviteCode = { code -> onAction(GlobalPositionAction.OnCopyInviteCode(code)) }
                        )
                    }
                    item(key = "club_join") {
                        JoinClubCard(onClick = { onAction(GlobalPositionAction.OnJoinClubClick) })
                    }
                }
            }

            if (state.nextMatch != null) {
                item(key = "next_match_header") {
                    SectionHeader(
                        title = "Próximo Partido",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 24.dp, bottom = 12.dp)
                    )
                }
                item(key = "next_match") {
                    NextMatchCard(
                        match = state.nextMatch,
                        isJoined = state.isJoinedNextMatch,
                        isLoading = state.isTogglingParticipation,
                        onToggleParticipation = {
                            onAction(OnToggleMatchParticipation(state.nextMatch.id))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            if (state.recentMatches.isNotEmpty()) {
                item(key = "matches_header") {
                    SectionHeader(
                        title = "Últimos Partidos",
                        actionLabel = "Ver todos",
                        onActionClick = { /* TODO: navigate to matches */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 24.dp, bottom = 12.dp)
                    )
                }
                items(
                    items = state.recentMatches,
                    key = { "match_${it.id}" }
                ) { match ->
                    MatchCard(
                        match = match,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 10.dp)
                    )
                }
            }

            if (state.news.isNotEmpty()) {
                item(key = "news_header") {
                    SectionHeader(
                        title = "Últimas Noticias",
                        actionLabel = "Ver todos",
                        onActionClick = { /* TODO: navigate to news */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 24.dp, bottom = 12.dp)
                    )
                }
                items(
                    items = state.news,
                    key = { "news_${it.id}" }
                ) { newsItem ->
                    NewsCard(
                        news = newsItem,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 10.dp)
                    )
                }
            }
        }
    }
}
