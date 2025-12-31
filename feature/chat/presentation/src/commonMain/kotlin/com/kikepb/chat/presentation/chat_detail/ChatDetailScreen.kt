@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalUuidApi::class)

package com.kikepb.chat.presentation.chat_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnBackClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnChatMembersClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnChatOptionsClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnDeleteMessageClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnDismissChatOptions
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnDismissMessageMenu
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnFirstVisibleIndexChanged
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnHideBanner
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnLeaveChatClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnMessageLongClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnRetryClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnRetryPaginationClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnTopVisibleIndexChanged
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnScrollToTop
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnSelectChat
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnSendMessageClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailEvent.OnChatLeft
import com.kikepb.chat.presentation.chat_detail.ChatDetailEvent.OnError
import com.kikepb.chat.presentation.chat_detail.ChatDetailEvent.OnNewMessage
import com.kikepb.chat.presentation.chat_detail.components.ChatDetailHeader
import com.kikepb.chat.presentation.chat_detail.components.MessageBox
import com.kikepb.chat.presentation.chat_detail.components.MessageList
import com.kikepb.chat.presentation.chat_detail.components.SquadfyDate
import com.kikepb.chat.presentation.components.ChatHeader
import com.kikepb.chat.presentation.components.EmptySection
import com.kikepb.chat.presentation.components.MessageBannerListener
import com.kikepb.chat.presentation.components.PaginationScrollListener
import com.kikepb.chat.presentation.model.ChatModelUi
import com.kikepb.chat.presentation.model.MessageModelUi.LocalUserMessage
import com.kikepb.chat.presentation.model.MessageModelUi.OtherUserMessage
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import com.kikepb.core.presentation.util.UiText
import com.kikepb.core.presentation.util.clearFocusOnTap
import com.kikepb.core.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.chat.presentation.generated.resources.no_chat_selected
import squadfy_app.feature.chat.presentation.generated.resources.select_a_chat
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
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
    val messageListState = rememberLazyListState()
    val snackBarState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            OnChatLeft -> onBack()
            OnNewMessage -> {
                scope.launch {
                    messageListState.animateScrollToItem(index = 0)
                }
            }
            is OnError -> snackBarState.showSnackbar(event.error.asStringAsync())
        }
    }

    LaunchedEffect(key1 = chatId) {
        viewModel.onAction(action = OnSelectChat(chatId = chatId))
    }

    LaunchedEffect(key1 = chatId) {
        if (chatId != null) messageListState.scrollToItem(index = 10)
    }

    BackHandler(enabled = !isDetailPresent) {
        scope.launch {
            /*
            Add artificial delay to prevent detail back animation from showing
            an unselected chat the moment we go back
            */
            delay(timeMillis = 300)
            viewModel.onAction(action = OnSelectChat(chatId = null))
        }
        onBack()
    }

    ChatDetailScreen(
        state = state,
        messageListState = messageListState,
        isDetailPresent = isDetailPresent,
        snackBarState = snackBarState,
        onAction = { action ->
            when (action) {
                is OnChatMembersClick -> onChatMembersClick()
                is OnBackClick -> onBack()
                else -> Unit
            }
            viewModel.onAction(action = action)
        }
    )
}

@Composable
fun ChatDetailScreen(
    state: ChatDetailState,
    messageListState: LazyListState,
    isDetailPresent: Boolean,
    snackBarState: SnackbarHostState,
    onAction: (ChatDetailAction) -> Unit,
) {
    var headerHeight by remember { mutableStateOf(value = 0.dp) }
    val density = LocalDensity.current
    val configuration = currentDeviceConfiguration()
    val realMessageItemCount = remember(key1 = state.messages) {
        state.messages.filter { it is LocalUserMessage || it is OtherUserMessage }.size
    }

    LaunchedEffect(key1 = messageListState) {
        snapshotFlow {
            messageListState.firstVisibleItemIndex to messageListState.layoutInfo.totalItemsCount
        }.filter { (firstVisibleIndex, totalItemsCount) ->
            firstVisibleIndex >= 0 && totalItemsCount > 0
        }.collect { (firstVisibleItemIndex, _) ->
            onAction(OnFirstVisibleIndexChanged(index = firstVisibleItemIndex))
        }
    }

    MessageBannerListener(
        lazyListState = messageListState,
        messages = state.messages,
        isBannerVisible = state.bannerState.isVisible,
        onShowBanner = { index ->
            onAction(OnTopVisibleIndexChanged(topVisibleIndex = index))
        },
        onHide = { onAction(OnHideBanner)}
    )

    PaginationScrollListener(
        lazyListState = messageListState,
        itemCount = realMessageItemCount,
        isPaginationLoading = state.isPaginationLoading,
        isEndReached = state.endReached,
        onNearTop = { onAction(OnScrollToTop) }
    )

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
                    if (state.chatUi == null) {
                        EmptySection(
                            title = stringResource(RString.no_chat_selected),
                            description = stringResource(RString.select_a_chat),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ChatHeader(
                            modifier = Modifier.onSizeChanged {
                                headerHeight = with(receiver = density) {
                                    it.height.toDp()
                                }
                            }
                        ) {
                            ChatDetailHeader(
                                chatUi = state.chatUi,
                                isDetailPresent = isDetailPresent,
                                isChatOptionsDropDownOpen = state.isChatOptionsOpen,
                                onChatOptionsClick = { onAction(OnChatOptionsClick) },
                                onDismissChatOptions = { onAction(OnDismissChatOptions) },
                                onManageChatClick = { onAction(OnChatMembersClick) },
                                onLeaveChatClick = { onAction(OnLeaveChatClick) },
                                onBackClick = { onAction(OnBackClick) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        MessageList(
                            messages = state.messages,
                            messageWithOpenMenu = state.messageWithOpenMenu,
                            listState = messageListState,
                            isPaginationLoading = state.isPaginationLoading,
                            paginationError = state.paginationError?.asString(),
                            onMessageLongClick = { message ->
                                onAction(OnMessageLongClick(message))
                            },
                            onMessageRetryClick = { message ->
                                onAction(OnRetryClick(message))
                            },
                            onDismissMessageMenu = {
                                onAction(OnDismissMessageMenu)
                            },
                            onDeleteMessageClick = { message ->
                                onAction(OnDeleteMessageClick(message))
                            },
                            onRetryPaginationClick = {
                                onAction(OnRetryPaginationClick)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )

                        AnimatedVisibility(visible = !configuration.isWideScreen) {
                            MessageBox(
                                messageTextFieldState = state.messageTextFieldState,
                                isSendButtonEnabled = state.canSendMessage,
                                connectionState = state.connectionState,
                                onSendClick = { onAction(OnSendMessageClick) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                            )
                        }
                    }
                }

                if(configuration.isWideScreen) Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = configuration.isWideScreen && state.chatUi != null
                ) {
                    DynamicRoundedCornerColumn(isCornersRounded = configuration.isWideScreen) {
                        MessageBox(
                            messageTextFieldState = state.messageTextFieldState,
                            isSendButtonEnabled = state.canSendMessage,
                            connectionState = state.connectionState,
                            onSendClick = { onAction(OnSendMessageClick) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 8.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = state.bannerState.isVisible,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = headerHeight + 16.dp),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (state.bannerState.formattedDate != null) SquadfyDate(date = state.bannerState.formattedDate.asString())
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
            messageListState = LazyListState(),
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
                        LocalUserMessage(
                            id = Uuid.random().toString(),
                            content = "Hello world!",
                            deliveryStatus = ChatMessageDeliveryStatus.SENT,
                            formattedSentTime = UiText.DynamicString("Friday, Aug 20")
                        )
                    } else {
                        OtherUserMessage(
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
            messageListState = LazyListState(),
            isDetailPresent = true,
            snackBarState = remember { SnackbarHostState() },
            onAction = {}
        )
    }
}