package com.kikepb.chat.presentation.chat_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.chat.presentation.chat_list.ChatListAction.OnConfirmLogout
import com.kikepb.chat.presentation.chat_list.ChatListAction.OnCreateChatClick
import com.kikepb.chat.presentation.chat_list.ChatListAction.OnProfileSettingsClick
import com.kikepb.chat.presentation.chat_list.ChatListAction.OnSelectChat
import com.kikepb.chat.presentation.chat_list.ChatListEvent.OnLogoutError
import com.kikepb.chat.presentation.chat_list.ChatListEvent.OnLogoutSuccess
import com.kikepb.chat.presentation.chat_list.components.ChatListHeader
import com.kikepb.chat.presentation.chat_list.components.ChatListItemUi
import com.kikepb.chat.presentation.components.EmptySection
import com.kikepb.core.designsystem.components.buttons.SquadfyFloatingActionButton
import com.kikepb.core.designsystem.components.dialogs.SquadfyDestructiveConfirmationDialog
import com.kikepb.core.designsystem.components.divider.SquadfyHorizontalDivider
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.permissions.Permission
import com.kikepb.core.presentation.permissions.rememberPermissionController
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.chat.presentation.generated.resources.cancel
import squadfy_app.feature.chat.presentation.generated.resources.create_chat
import squadfy_app.feature.chat.presentation.generated.resources.do_you_want_to_logout
import squadfy_app.feature.chat.presentation.generated.resources.do_you_want_to_logout_desc
import squadfy_app.feature.chat.presentation.generated.resources.logout
import squadfy_app.feature.chat.presentation.generated.resources.no_chats
import squadfy_app.feature.chat.presentation.generated.resources.no_chats_subtitle
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString

@Composable
fun ChatListRoot(
    selectedChatId: String?,
    onChatClick: (String?) -> Unit,
    onSuccessfulLogout: () -> Unit,
    onCreateChatClick: () -> Unit,
    onProfileSettingsClick: () -> Unit,
    viewModel: ChatListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = selectedChatId) {
        viewModel.onAction(action = OnSelectChat(chatId = selectedChatId))
    }

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is OnLogoutError -> {
                scope.launch {
                    snackBarHostState.showSnackbar(message = event.error.asStringAsync())
                }
            }
            OnLogoutSuccess -> onSuccessfulLogout()
        }
    }

    ChatListScreen(
        state = state,
        onAction = { action ->
            when(action) {
                is OnSelectChat -> onChatClick(action.chatId)
                OnConfirmLogout -> onSuccessfulLogout()
                OnCreateChatClick -> onCreateChatClick()
                OnProfileSettingsClick -> onProfileSettingsClick()
                else -> Unit
            }
            viewModel.onAction(action)
        },
        snackBarHostState = snackBarHostState
    )
}

@Composable
fun ChatListScreen(
    state: ChatListState,
    onAction: (ChatListAction) -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val permissionController = rememberPermissionController()

    LaunchedEffect(key1 = true) {
        permissionController.requestPermission(permission = Permission.NOTIFICATIONS)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            SquadfyFloatingActionButton(
                onClick = {
                    onAction(OnCreateChatClick)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(RString.create_chat)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChatListHeader(
                localParticipant = state.localParticipant,
                isUserMenuOpen = state.isUserMenuOpen,
                onUserAvatarClick = { onAction(ChatListAction.OnUserAvatarClick) },
                onLogoutClick = { onAction(ChatListAction.OnLogoutClick) },
                onDismissMenu = { onAction(ChatListAction.OnDismissUserMenu) },
                onProfileSettingsClick = { onAction(OnProfileSettingsClick) }
            )
            when {
                state.isLoading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                state.chats.isEmpty() -> {
                    EmptySection(
                        title = stringResource(RString.no_chats),
                        description = stringResource(RString.no_chats_subtitle),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(
                            items = state.chats,
                            key = { it.id }
                        ) { chatUi ->
                            ChatListItemUi(
                                chat = chatUi,
                                isSelected = chatUi.id == state.selectedChatId,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAction(OnSelectChat(chatUi.id))
                                    }
                            )
                            SquadfyHorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    if(state.showLogoutConfirmation) {
        SquadfyDestructiveConfirmationDialog(
            title = stringResource(RString.do_you_want_to_logout),
            description = stringResource(RString.do_you_want_to_logout_desc),
            confirmButtonText = stringResource(RString.logout),
            cancelButtonText = stringResource(RString.cancel),
            onDismiss = {
                onAction(ChatListAction.OnDismissLogoutDialog)
            },
            onCancelClick = {
                onAction(ChatListAction.OnDismissLogoutDialog)
            },
            onConfirmClick = {
                onAction(OnConfirmLogout)
            },
        )
    }
}

@Preview
@Composable
private fun ChatListPreview() {
    SquadfyTheme {
        ChatListScreen(
            state = ChatListState(),
            onAction = {},
            snackBarHostState = remember { SnackbarHostState() }
        )
    }
}