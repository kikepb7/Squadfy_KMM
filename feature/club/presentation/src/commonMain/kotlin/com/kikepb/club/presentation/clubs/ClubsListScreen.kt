package com.kikepb.club.presentation.clubs

import com.kikepb.core.designsystem.components.loading.SquadfyLoadingIndicator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.MyClubModel
import com.kikepb.club.presentation.mapper.initialsOf
import com.kikepb.club.presentation.mapper.label
import com.kikepb.core.designsystem.components.avatar.SquadfyAvatarPhoto
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.clubs_create
import squadfy_app.feature.club.presentation.generated.resources.clubs_empty_description
import squadfy_app.feature.club.presentation.generated.resources.clubs_empty_title
import squadfy_app.feature.club.presentation.generated.resources.clubs_join
import squadfy_app.feature.club.presentation.generated.resources.clubs_members_count
import squadfy_app.feature.club.presentation.generated.resources.clubs_members_count_limit
import squadfy_app.feature.club.presentation.generated.resources.clubs_title

@Composable
fun ClubsListRoot(
    onClubClick: (clubId: String) -> Unit,
    onJoinClub: () -> Unit,
    onCreateClub: () -> Unit,
    viewModel: ClubsListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ClubsListEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
        }
    }

    ClubsListScreen(
        state = state,
        onAction = viewModel::onAction,
        onClubClick = onClubClick,
        onJoinClub = onJoinClub,
        onCreateClub = onCreateClub,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubsListScreen(
    state: ClubsListState,
    onAction: (ClubsListAction) -> Unit,
    onClubClick: (clubId: String) -> Unit,
    onJoinClub: () -> Unit,
    onCreateClub: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        topBar = { SquadfyTopBar(title = stringResource(Res.string.clubs_title)) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(ClubsListAction.OnRefresh) },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.clubs.isEmpty() && state.isLoading) item(key = "loading") { SquadfyLoadingIndicator() }
                if (state.clubs.isEmpty() && !state.isLoading) {
                    item(key = "empty") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                text = stringResource(Res.string.clubs_empty_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.extended.textPrimary
                            )
                            Text(
                                text = stringResource(Res.string.clubs_empty_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.extended.textPlaceholder
                            )
                        }
                    }
                }
                items(items = state.clubs, key = { it.club.id }) { myClub ->
                    MyClubCard(myClub = myClub, onClick = { onClubClick(myClub.club.id) })
                }
                item(key = "join") {
                    ClubOptionCard(title = stringResource(Res.string.clubs_join), icon = Icons.Outlined.Person, onClick = onJoinClub)
                }
                item(key = "create") {
                    ClubOptionCard(title = stringResource(Res.string.clubs_create), icon = Icons.Outlined.Add, onClick = onCreateClub)
                }
            }
        }
    }
}

@Composable
private fun MyClubCard(myClub: MyClubModel, onClick: () -> Unit) {
    val club = myClub.club
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SquadfyAvatarPhoto(displayText = initialsOf(club.name), imageUrl = club.clubLogoUrl)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = club.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
                val members = club.maxMembers
                    ?.let { stringResource(Res.string.clubs_members_count_limit, club.membersCount, it) }
                    ?: stringResource(Res.string.clubs_members_count, club.membersCount)
                val role = myClub.myRole?.let { " · ${stringResource(it.label)}" }.orEmpty()
                Text(
                    text = members + role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.extended.textPlaceholder
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.extended.textPlaceholder
            )
        }
    }
}

@Composable
private fun ClubOptionCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
