package com.kikepb.chat.presentation.chat_list

import androidx.compose.material3.Text
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.input.rememberTextFieldState
import com.kikepb.core.designsystem.components.loading.SquadfyLoadingIndicator
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBarIconButton
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import squadfy_app.feature.chat.presentation.generated.resources.chat_search
import squadfy_app.feature.chat.presentation.generated.resources.chat_search_empty
import squadfy_app.feature.chat.presentation.generated.resources.profile_settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
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
import squadfy_app.feature.chat.presentation.generated.resources.chat_list_title
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

    val query = rememberTextFieldState()
    val needle = query.text.toString().trim()
    val chats = if (needle.isBlank()) state.chats else state.chats.filter { chat ->
        chat.otherParticipants.any { it.username.contains(needle, ignoreCase = true) } ||
            chat.lastMessage?.content?.contains(needle, ignoreCase = true) == true
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Triangulacion.colors.surface100,
        // Spec 018 (screen 14): title, «+» to create a chat and the profile behind the settings button
        topBar = {
            SquadfyTopBar(
                title = { Text(text = stringResource(RString.chat_list_title), style = Triangulacion.type.displayLg, color = Triangulacion.colors.ink) },
                actions = {
                    SquadfyTopBarIconButton(onClick = { onAction(OnCreateChatClick) }, icon = TriIcon.Plus, contentDescription = stringResource(RString.create_chat))
                    SquadfyTopBarIconButton(onClick = { onAction(OnProfileSettingsClick) }, icon = TriIcon.User, contentDescription = stringResource(RString.profile_settings))
                }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                state.isLoading -> SquadfyLoadingIndicator()
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
                    SquadfyTextField(
                        state = query,
                        placeholder = stringResource(RString.chat_search),
                        singleLine = true,
                        leadingIcon = TriIcon.Search,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Space.s4, vertical = Space.s2)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = Space.s4, vertical = Space.s2),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (chats.isEmpty()) item(key = "no-match") {
                            Text(text = stringResource(RString.chat_search_empty), style = Triangulacion.type.body, color = Triangulacion.colors.inkMuted)
                        }
                        items(
                            items = chats,
                            key = { it.id }
                        ) { chatUi ->
                            ChatListItemUi(
                                chat = chatUi,
                                isSelected = chatUi.id == state.selectedChatId,
                                onClick = { onAction(OnSelectChat(chatUi.id)) },
                                modifier = Modifier.fillMaxWidth()
                            )
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