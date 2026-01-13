@file:OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)

package com.kikepb.chat.presentation.chat_list_detail

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole.Detail
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole.List
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue.Companion.Expanded
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue.Companion.Hidden
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.chat.presentation.chat_detail.ChatDetailRoot
import com.kikepb.chat.presentation.chat_list.ChatListRoot
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAction.OnSelectChat
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAction.OnCreateChatClick
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAction.OnDismissCurrentDialog
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAction.OnProfileSettingsClick
import com.kikepb.chat.presentation.create_chat.CreateChatRoot
import com.kikepb.chat.presentation.manage_chat.ManageChatRoot
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.DialogSheetScopedViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(FlowPreview::class)
@Composable
fun ChatListDetailAdaptiveLayout(
    initialChatId: String?,
    chatListDetailViewModel: ChatListDetailViewModel = koinViewModel(),
    onLogout: () -> Unit
) {
    val sharedState by chatListDetailViewModel.state.collectAsStateWithLifecycle()
    val scaffoldDirective = createNoSpacingPaneScaffoldDirective()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator(
        scaffoldDirective = scaffoldDirective
    )
    val scope = rememberCoroutineScope()
    val detailPane = scaffoldNavigator.scaffoldValue[Detail]

    LaunchedEffect(key1 = initialChatId) {
        if (initialChatId != null) {
            chatListDetailViewModel.onAction(action = OnSelectChat(chatId = initialChatId))
            scaffoldNavigator.navigateTo(pane = Detail)
        }
    }

    LaunchedEffect(key1 = detailPane, key2 = sharedState.selectedChatId) {
        if (detailPane == Hidden && sharedState.selectedChatId != null) {
            chatListDetailViewModel.onAction(action = OnSelectChat(chatId = null))
        }
    }

    BackHandler(enabled = scaffoldNavigator.canNavigateBack()) {
        scope.launch {
            scaffoldNavigator.navigateBack()
            chatListDetailViewModel.onAction(action = OnSelectChat(chatId = null))
        }
    }

    ListDetailPaneScaffold(
        directive = scaffoldDirective,
        value = scaffoldNavigator.scaffoldValue,
        modifier = Modifier.background(color = MaterialTheme.colorScheme.extended.surfaceLower),
        listPane = {
            AnimatedPane {
                ChatListRoot(
                    selectedChatId = sharedState.selectedChatId,
                    onChatClick = {
                        chatListDetailViewModel.onAction(action = OnSelectChat(chatId = it))
                        scope.launch { scaffoldNavigator.navigateTo(Detail) }
                    },
                    onSuccessfulLogout = { onLogout() },
                    onCreateChatClick = { chatListDetailViewModel.onAction(action = OnCreateChatClick) },
                    onProfileSettingsClick = { chatListDetailViewModel.onAction(action = OnProfileSettingsClick) }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val listPane = scaffoldNavigator.scaffoldValue[List]

                ChatDetailRoot(
                    chatId = sharedState.selectedChatId,
                    isDetailPresent = detailPane == Expanded && listPane == Expanded,
                    onChatMembersClick = {
                        chatListDetailViewModel.onAction(action = ChatListDetailAction.OnManageChatClick)
                    },
                    onBack = {
                        scope.launch {
                            if (scaffoldNavigator.canNavigateBack()) scaffoldNavigator.navigateBack()
                        }
                    }
                )
            }
        }
    )

    DialogSheetScopedViewModel(visible = sharedState.dialogState is DialogState.CreateChat) {
        CreateChatRoot(
            onChatCreated = { chat ->
                chatListDetailViewModel.onAction(action = OnDismissCurrentDialog)
                chatListDetailViewModel.onAction(action = OnSelectChat(chatId = chat.id))
                scope.launch {
                    scaffoldNavigator.navigateTo(pane = Detail)
                }
            },
            onDismiss = {
                chatListDetailViewModel.onAction(action = OnDismissCurrentDialog)
            }
        )
    }

    DialogSheetScopedViewModel(visible = sharedState.dialogState is DialogState.ManageChat) {
        ManageChatRoot(
            chatId = sharedState.selectedChatId,
            onMembersAdded = { chatListDetailViewModel.onAction(action = OnDismissCurrentDialog) },
            onDismiss = { chatListDetailViewModel.onAction(action = OnDismissCurrentDialog) }
        )
    }
}