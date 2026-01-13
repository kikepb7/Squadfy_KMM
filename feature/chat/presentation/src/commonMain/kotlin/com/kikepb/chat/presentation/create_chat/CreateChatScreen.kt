@file:OptIn(FlowPreview::class)

package com.kikepb.chat.presentation.create_chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.chat.presentation.components.manage_chat.ManageChatScreen
import com.kikepb.chat.presentation.create_chat.ManageChatAction.OnDismissDialog
import com.kikepb.chat.presentation.create_chat.CreateChatEvent.OnChatCreated
import com.kikepb.core.designsystem.components.dialogs.SquadfyAdaptiveDialogSheetLayout
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.FlowPreview
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.chat.presentation.generated.resources.create_chat
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString

@Composable
fun CreateChatRoot(
    onDismiss: () -> Unit,
    onChatCreated: (ChatModel) -> Unit,
    viewModel: CreateChatViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is OnChatCreated -> onChatCreated(event.chat)
        }
    }

    SquadfyAdaptiveDialogSheetLayout(onDismiss = onDismiss) {
        ManageChatScreen(
            headerText = stringResource(RString.create_chat),
            primaryButtonText = stringResource(RString.create_chat),
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

@Preview
@Composable
private fun CreateChatPreview() {
    SquadfyTheme {
        ManageChatScreen(
            headerText = "Create Chat",
            primaryButtonText = "Create Chat",
            state = ManageChatState(),
            onAction = {}
        )
    }
}