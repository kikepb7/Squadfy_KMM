@file:OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)

package com.kikepb.chat.presentation.chat_list_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole.Detail
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.chat.presentation.chat_list.ChatListRoot
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAction.OnChatClick
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAction.OnCreateChatClick
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAction.OnDismissCurrentDialog
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAction.OnProfileSettingsClick
import com.kikepb.chat.presentation.create_chat.CreateChatRoot
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.DialogSheetScopedViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(FlowPreview::class)
@Composable
fun ChatListDetailAdaptiveLayout(
    chatListDetailViewModel: ChatListDetailViewModel = koinViewModel(),
    onLogout: () -> Unit
) {
    val sharedState by chatListDetailViewModel.state.collectAsStateWithLifecycle()
    val scaffoldDirective = createNoSpacingPaneScaffoldDirective()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator(
        scaffoldDirective = scaffoldDirective
    )
    val scope = rememberCoroutineScope()

    BackHandler(enabled = scaffoldNavigator.canNavigateBack()) {
        scope.launch {
            scaffoldNavigator.navigateBack()
        }
    }

    ListDetailPaneScaffold(
        directive = scaffoldDirective,
        value = scaffoldNavigator.scaffoldValue,
        modifier = Modifier.background(color = MaterialTheme.colorScheme.extended.surfaceLower),
        listPane = {
            AnimatedPane {
                ChatListRoot(
                    onChatClick = {
                        chatListDetailViewModel.onAction(action = OnChatClick(chatId = it.id))
                        scope.launch { scaffoldNavigator.navigateTo(Detail) }
                    },
                    onConfirmLogoutClick = { onLogout() },
                    onCreateChatClick = { chatListDetailViewModel.onAction(action = OnCreateChatClick) },
                    onProfileSettingsClick = { chatListDetailViewModel.onAction(action = OnProfileSettingsClick) }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    sharedState.selectedChatId?.let {
                        Text(text = it)
                    }
                }
            }
        }
    )

    DialogSheetScopedViewModel(
        visible = sharedState.dialogState is DialogState.CreateChat
    ) {
        CreateChatRoot(
            onChatCreated = { chat ->
                chatListDetailViewModel.onAction(action = OnDismissCurrentDialog)
                chatListDetailViewModel.onAction(action = OnChatClick(chatId = chat.id))
                scope.launch {
                    scaffoldNavigator.navigateTo(pane = Detail)
                }
            },
            onDismiss = {
                chatListDetailViewModel.onAction(action = OnDismissCurrentDialog)
            }
        )
    }
}