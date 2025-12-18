package com.kikepb.chat.presentation.manage_chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.chat.presentation.components.manage_chat.ManageChatScreen
import com.kikepb.chat.presentation.create_chat.ManageChatAction.ChatParticipants.OnSelectChat
import com.kikepb.chat.presentation.create_chat.ManageChatAction.OnDismissDialog
import com.kikepb.core.designsystem.components.dialogs.SquadfyAdaptiveDialogSheetLayout
import com.kikepb.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.chat.presentation.generated.resources.chat_members
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.chat.presentation.generated.resources.save

@Composable
fun ManageChatRoot(
    chatId: String?,
    onDismiss: () -> Unit,
    onMembersAdded: () -> Unit,
    viewModel: ManageChatViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = chatId) {
        viewModel.onAction(action = OnSelectChat(chatId = chatId))
    }

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is ManageChatEvent.OnMembersAdded -> onMembersAdded()
        }
    }

    SquadfyAdaptiveDialogSheetLayout(onDismiss = onDismiss) {
        ManageChatScreen(
            headerText = stringResource(RString.chat_members),
            primaryButtonText = stringResource(RString.save),
            state = state,
            onAction = { action ->
                when (action) {
                    OnDismissDialog -> onDismiss()
                    else -> Unit
                }

                viewModel.onAction(action = action)
            }
        )
    }
}