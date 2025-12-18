@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalUuidApi::class)

package com.kikepb.chat.presentation.chat_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnSelectChat
import com.kikepb.chat.presentation.chat_detail.ChatDetailEvent.OnChatLeft
import com.kikepb.chat.presentation.chat_detail.ChatDetailEvent.OnError
import com.kikepb.chat.presentation.chat_detail.components.ChatDetailHeader
import com.kikepb.chat.presentation.chat_detail.components.MessageBox
import com.kikepb.chat.presentation.chat_detail.components.MessageList
import com.kikepb.chat.presentation.components.ChatHeader
import com.kikepb.chat.presentation.model.ChatModelUi
import com.kikepb.chat.presentation.model.MessageModelUi
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import com.kikepb.core.presentation.util.UiText
import com.kikepb.core.presentation.util.clearFocusOnTap
import com.kikepb.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun ChatDetailRoot(
    chatId: String?,
    isDetailPresent: Boolean,
    onBack: () -> Unit,
    onChatMembersClick: () -> Unit,
    viewModel: ChatDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackBarState= remember { SnackbarHostState() }

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            OnChatLeft -> onBack()
            is OnError -> snackBarState.showSnackbar(event.error.asStringAsync())
        }
    }

    LaunchedEffect(key1 = chatId) {
        viewModel.onAction(action = OnSelectChat(chatId = chatId))
    }

    BackHandler(enabled = !isDetailPresent) {
        viewModel.onAction(action = OnSelectChat(chatId = null))
        onBack()
    }

    ChatDetailScreen(
        state = state,
        isDetailPresent = isDetailPresent,
        snackBarState = snackBarState,
        onAction = { action ->
            when (action) {
                is ChatDetailAction.OnChatMembersClick -> onChatMembersClick()
                else -> Unit
            }
            viewModel.onAction(action = action)
        }
    )
}

@Composable
fun ChatDetailScreen(
    state: ChatDetailState,
    isDetailPresent: Boolean,
    snackBarState: SnackbarHostState,
    onAction: (ChatDetailAction) -> Unit,
) {
    val configuration = currentDeviceConfiguration()
    val messageListState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor =
            if (!configuration.isWideScreen) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.extended.surfaceLower,
        snackbarHost = { SnackbarHost(hostState = snackBarState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .clearFocusOnTap()
                .padding(innerPadding)
                .then(
                    if (configuration.isWideScreen) Modifier.padding(horizontal = 8.dp) else Modifier
                )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DynamicRoundedCornerColumn(
                    isCornersRounded = configuration.isWideScreen,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    ChatHeader {
                        ChatDetailHeader(
                            chatUi = state.chatUi,
                            isDetailPresent = isDetailPresent,
                            isChatOptionsDropDownOpen = state.isChatOptionsOpen,
                            onChatOptionsClick = {
                                onAction(ChatDetailAction.OnChatOptionsClick)
                            },
                            onDismissChatOptions = {
                                onAction(ChatDetailAction.OnDismissChatOptions)
                            },
                            onManageChatClick = {
                                onAction(ChatDetailAction.OnChatMembersClick)
                            },
                            onLeaveChatClick = {
                                onAction(ChatDetailAction.OnLeaveChatClick)
                            },
                            onBackClick = {
                                onAction(ChatDetailAction.OnBackClick)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    MessageList(
                        messages = state.messages,
                        listState = messageListState,
                        onMessageLongClick = { message ->
                            onAction(ChatDetailAction.OnMessageLongClick(message))
                        },
                        onMessageRetryClick = { message ->
                            onAction(ChatDetailAction.OnRetryClick(message))
                        },
                        onDismissMessageMenu = {
                            onAction(ChatDetailAction.OnDismissMessageMenu)
                        },
                        onDeleteMessageClick = { message ->
                            onAction(ChatDetailAction.OnDeleteMessageClick(message))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    AnimatedVisibility(
                        visible = !configuration.isWideScreen && state.chatUi != null
                    ) {
                        MessageBox(
                            messageTextFieldState = state.messageTextFieldState,
                            isTextInputEnabled = state.canSendMessage,
                            connectionState = state.connectionState,
                            onSendClick = { onAction(ChatDetailAction.OnSendMessageClick) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        )
                    }
                }

                if(configuration.isWideScreen) Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = configuration.isWideScreen && state.chatUi != null
                ) {
                    DynamicRoundedCornerColumn(isCornersRounded = configuration.isWideScreen) {
                        MessageBox(
                            messageTextFieldState = state.messageTextFieldState,
                            isTextInputEnabled = state.canSendMessage,
                            connectionState = state.connectionState,
                            onSendClick = { onAction(ChatDetailAction.OnSendMessageClick) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicRoundedCornerColumn(
    isCornersRounded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = if (isCornersRounded) 4.dp else 0.dp,
                shape = if (isCornersRounded) RoundedCornerShape(24.dp) else RectangleShape,
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = if (isCornersRounded) RoundedCornerShape(24.dp) else RectangleShape
            )
    ) {
        content()
    }
}

@Preview
@Composable
private fun ChatDetailEmptyPreview() {
    SquadfyTheme {
        ChatDetailScreen(
            state = ChatDetailState(),
            isDetailPresent = false,
            snackBarState = remember { SnackbarHostState() },
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun ChatDetailMessagesPreview() {
    SquadfyTheme(darkTheme = true) {
        ChatDetailScreen(
            state = ChatDetailState(
                messageTextFieldState = rememberTextFieldState(
                    initialText = "This is a new message!"
                ),
                canSendMessage = true,
                chatUi = ChatModelUi(
                    id = "1",
                    localParticipant = ChatParticipantModelUi(
                        id = "1",
                        username = "Enrique",
                        initials = "EN",
                    ),
                    otherParticipants = listOf(
                        ChatParticipantModelUi(
                            id = "2",
                            username = "Carmen",
                            initials = "CA",
                        ),
                        ChatParticipantModelUi(
                            id = "3",
                            username = "Ana",
                            initials = "AN",
                        )
                    ),
                    lastMessage = ChatMessageModel(
                        id = "1",
                        chatId = "1",
                        content = "This is a last chat message that was sent by Enrique " +
                                "and goes over multiple lines to showcase the ellipsis",
                        createdAt = Clock.System.now(),
                        senderId = "1",
                        deliveryStatus = ChatMessageDeliveryStatus.SENT
                    ),
                    lastMessageSenderUsername = "Enrique"
                ),
                messages = (1..20).map {
                    if(it % 2 == 0) {
                        MessageModelUi.LocalUserMessage(
                            id = Uuid.random().toString(),
                            content = "Hello world!",
                            deliveryStatus = ChatMessageDeliveryStatus.SENT,
                            isMenuOpen = false,
                            formattedSentTime = UiText.DynamicString("Friday, Aug 20")
                        )
                    } else {
                        MessageModelUi.OtherUserMessage(
                            id = Uuid.random().toString(),
                            content = "Hello world!",
                            sender = ChatParticipantModelUi(
                                id = Uuid.random().toString(),
                                username = "Carmen",
                                initials = "CA"
                            ),
                            formattedSentTime = UiText.DynamicString("Friday, Aug 20"),
                        )
                    }
                }
            ),
            isDetailPresent = true,
            snackBarState = remember { SnackbarHostState() },
            onAction = {}
        )
    }
}