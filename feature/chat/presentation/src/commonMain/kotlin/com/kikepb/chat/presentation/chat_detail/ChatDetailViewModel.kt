@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)

package com.kikepb.chat.presentation.chat_detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.models.ConnectionStateModel
import com.kikepb.chat.domain.models.ConnectionStateModel.CONNECTED
import com.kikepb.chat.domain.models.ConnectionStateModel.DISCONNECTED
import com.kikepb.chat.domain.models.OutgoingNewMessageModel
import com.kikepb.chat.domain.repository.ChatConnectionClient
import com.kikepb.chat.domain.usecases.FetchChatByIdUseCase
import com.kikepb.chat.domain.usecases.GetChatInfoByIdUseCase
import com.kikepb.chat.domain.usecases.LeaveChatUseCase
import com.kikepb.chat.domain.usecases.message.DeleteMessageUseCase
import com.kikepb.chat.domain.usecases.message.FetchMessagesUseCase
import com.kikepb.chat.domain.usecases.message.GetMessagesForChatUseCase
import com.kikepb.chat.domain.usecases.message.RetryMessageUseCase
import com.kikepb.chat.domain.usecases.message.SendMessageUseCase
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
import com.kikepb.chat.presentation.chat_detail.ChatDetailEvent.OnError
import com.kikepb.chat.presentation.chat_detail.ChatDetailEvent.OnNewMessage
import com.kikepb.chat.presentation.mappers.toUi
import com.kikepb.chat.presentation.mappers.toUiList
import com.kikepb.chat.presentation.model.BannerState
import com.kikepb.chat.presentation.model.ChatModelUi
import com.kikepb.chat.presentation.model.MessageModelUi
import com.kikepb.chat.presentation.model.MessageModelUi.DateSeparator
import com.kikepb.chat.presentation.model.MessageModelUi.LocalUserMessage
import com.kikepb.chat.presentation.model.MessageModelUi.OtherUserMessage
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.util.DataErrorException
import com.kikepb.core.domain.util.Paginator
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import com.kikepb.core.presentation.mapper.toUiText
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import squadfy_app.feature.chat.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.chat.presentation.generated.resources.today
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatDetailViewModel(
    private val fetchChatByIdUseCase: FetchChatByIdUseCase,
    private val getChatInfoByIdUseCase: GetChatInfoByIdUseCase,
    private val leaveChatUseCase: LeaveChatUseCase,
    private val fetchMessagesUseCase: FetchMessagesUseCase,
    private val getMessagesForChatUseCase: GetMessagesForChatUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val retryMessageUseCase: RetryMessageUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val chatConnectionClient: ChatConnectionClient,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private var currentPaginator: Paginator<String?, ChatMessageModel>? = null
    private var hasLoadedInitialData = false
    private val eventChannel = Channel<ChatDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _chatId = MutableStateFlow<String?>(value = null)
    private val chatInfoFlow = _chatId
        .onEach { chatId ->
            if (chatId != null) setUpPaginatorForChat(chatId = chatId) else currentPaginator = null
        }
        .flatMapLatest { chatId ->
            if (chatId != null) getChatInfoByIdUseCase.getChatInfoById(chatId = chatId) else emptyFlow()
        }
    private val _state = MutableStateFlow(ChatDetailState())
    val state = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) stateWithMessages else _state
        }
        .onStart {
            if (!hasLoadedInitialData) {
                observeConnectionState()
                observeChatMessages()
                observeCanSendMessage()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ChatDetailState()
        )
    private val canSendMessage = snapshotFlow { _state.value.messageTextFieldState.text.toString() }
        .map { it.isBlank() }
        .combine(chatConnectionClient.connectionState) { isMessageBlank, connectionState ->
            !isMessageBlank && connectionState == CONNECTED
        }
    private val stateWithMessages = combine(
        _state,
        chatInfoFlow,
        sessionStorage.observeAuthInfo()
    ) { currentState, chatInfo, authInfo ->
        if (authInfo == null) return@combine ChatDetailState()

        currentState.copy(
            chatUi = chatInfo.chat.toUi(localParticipantId = authInfo.user.id),
            messages = chatInfo.messages.toUiList(localUserId = authInfo.user.id)
        )
    }

    private fun switchChat(chatId: String?) {
        _chatId.update { chatId }
        viewModelScope.launch {
            chatId?.let {
                fetchChatByIdUseCase.fetchChatById(chatId = chatId)
            }
        }
    }

    private fun onLeaveChatClick() {
        val chatId = _chatId.value ?: return

        _state.update { it.copy(isChatOptionsOpen = false) }

        viewModelScope.launch {
            leaveChatUseCase.leaveChat(chatId = chatId)
                .onSuccess {
                    _state.value.messageTextFieldState.clearText()

                    _chatId.update { null }
                    _state.update {
                        it.copy(
                            chatUi = null,
                            messages = emptyList(),
                            bannerState = BannerState()
                        )
                    }
                }
                .onFailure { error ->
                    eventChannel.send(OnError(error.toUiText()))
                }
        }
    }

    private fun observeConnectionState() {
        chatConnectionClient
            .connectionState
            .onEach { connectionState ->
                if (connectionState == CONNECTED) currentPaginator?.loadNextItems()

                _state.update { it.copy(connectionState = connectionState) }
            }.launchIn(scope = viewModelScope)
    }

    private fun observeChatMessages() {
        val currentMessages = state
            .map { it.messages }
            .distinctUntilChanged()

        val newMessages = _chatId.flatMapLatest { chatId ->
            if (chatId != null) getMessagesForChatUseCase.getMessagesForChat(chatId = chatId)
            else emptyFlow()
        }

        val isNearBottom = state.map { it.isNearBottom }.distinctUntilChanged()

        combine(
            flow = currentMessages,
            flow2 = newMessages,
            flow3 = isNearBottom
        ) { currentMessages, newMessages, isNearBottom ->
            val newestMessageId = newMessages.firstOrNull()?.message?.id
            val currentNewestId = currentMessages
                .asSequence()
                .filterNot { it is LocalUserMessage || it is OtherUserMessage }
                .firstOrNull()
                ?.id

            if (newestMessageId != null && newestMessageId != currentNewestId && isNearBottom) eventChannel.send(
                element = OnNewMessage
            )
        }.launchIn(scope = viewModelScope)
    }

    private fun sendMessage() {
        val currentChatId = _chatId.value
        val content = state.value.messageTextFieldState.text.toString().trim()
        if (content.isBlank() || currentChatId == null) return

        viewModelScope.launch {
            val message = OutgoingNewMessageModel(
                chatId = currentChatId,
                messageId = Uuid.random().toString(),
                content = content
            )

            sendMessageUseCase.sendMessage(message = message)
                .onSuccess { state.value.messageTextFieldState.clearText() }
                .onFailure { error ->
                    eventChannel.send(element = OnError(error = error.toUiText()))

                }
        }
    }

    private fun observeCanSendMessage() {
        canSendMessage.onEach { canSendMessage ->
            _state.update { it.copy(canSendMessage = canSendMessage) }
        }.launchIn(scope = viewModelScope)
    }

    private fun retryMessage(message: LocalUserMessage) {
        viewModelScope.launch {
            retryMessageUseCase.retryMessage(messageId = message.id)
                .onFailure { error ->
                    eventChannel.send(element = OnError(error = error.toUiText()))
                }
        }
    }

    private fun deleteMessage(message: LocalUserMessage) {
        viewModelScope.launch {
            deleteMessageUseCase.deleteMessage(messageId = message.id)
                .onFailure { error ->
                    eventChannel.send(element = OnError(error = error.toUiText()))
                }
        }
    }

    private fun onChatOptionsClick() = _state.update { it.copy(isChatOptionsOpen = true) }

    private fun onDismissChatOptions() = _state.update { it.copy(isChatOptionsOpen = false) }

    private fun onDismissMessageMenu() = _state.update { it.copy(messageWithOpenMenu = null) }

    private fun onMessageLongClick(message: LocalUserMessage) =
        _state.update { it.copy(messageWithOpenMenu = message) }

    private fun onScrollToTop() = loadNextItems()

    private fun onRetryPaginationClick() = loadNextItems()

    private fun onHideBanner() =
        _state.update { it.copy(bannerState = it.bannerState.copy(isVisible = false)) }

    private fun updateNearBottom(firstVisibleIndex: Int) = _state.update { it.copy(isNearBottom = firstVisibleIndex <= 3) }

    private fun updateBanner(topVisibleIndex: Int) {
        val visibleDate = calculateBannerDateFromIndex(messages = state.value.messages, index = topVisibleIndex)

        _state.update { it.copy(bannerState = BannerState(formattedDate = visibleDate, isVisible = visibleDate != null)) }
    }

    private fun calculateBannerDateFromIndex(messages: List<MessageModelUi>, index: Int): UiText? {
        if (messages.isEmpty() || index < 0 || index >= messages.size) return null

        val nearestDateSeparator = (index until messages.size)
            .asSequence()
            .mapNotNull { index ->
                val item = messages.getOrNull(index)
                if (item is DateSeparator) item.date else null
            }
            .firstOrNull()

        return when (nearestDateSeparator) {
            is UiText.Resource -> {
                if (nearestDateSeparator.id == RString.today) null else nearestDateSeparator
            }
            else -> nearestDateSeparator
        }
    }

    private fun loadNextItems() = viewModelScope.launch { currentPaginator?.loadNextItems() }

    private fun setUpPaginatorForChat(chatId: String) {
        currentPaginator = Paginator(
            initialKey = null,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isPaginationLoading = isLoading) }
            },
            onRequest = { beforeTimestamp ->
                fetchMessagesUseCase.fetchMessages(chatId = chatId, before = beforeTimestamp)
            },
            getNextKey = { messages ->
                messages.minOfOrNull { it.createdAt }?.toString()
            },
            onError = { throwable ->
                if (throwable is DataErrorException) _state.update { it.copy(paginationError = throwable.error.toUiText()) }
            },
            onSuccess = { messages, _ ->
                _state.update { it.copy(endReached = messages.isEmpty(), paginationError = null) }
            }
        )

        _state.update { it.copy(endReached = false, isPaginationLoading = false) }
    }

    fun onAction(action: ChatDetailAction) {
        when (action) {
            is OnSelectChat -> switchChat(chatId = action.chatId)
            OnChatOptionsClick -> onChatOptionsClick()
            is OnDeleteMessageClick -> deleteMessage(message = action.message)
            OnDismissChatOptions -> onDismissChatOptions()
            OnDismissMessageMenu -> onDismissMessageMenu()
            OnLeaveChatClick -> onLeaveChatClick()
            is OnMessageLongClick -> onMessageLongClick(message = action.message)
            is OnRetryClick -> retryMessage(message = action.message)
            OnScrollToTop -> onScrollToTop()
            OnSendMessageClick -> sendMessage()
            OnRetryPaginationClick -> onRetryPaginationClick()
            OnHideBanner -> onHideBanner()
            is OnFirstVisibleIndexChanged -> updateNearBottom(firstVisibleIndex = action.index)
            is OnTopVisibleIndexChanged -> updateBanner(topVisibleIndex = action.topVisibleIndex)
            else -> Unit
        }
    }
}

data class ChatDetailState(
    val chatUi: ChatModelUi? = null,
    val isLoading: Boolean = false,
    val messages: List<MessageModelUi> = emptyList(),
    val error: UiText? = null,
    val messageTextFieldState: TextFieldState = TextFieldState(),
    val canSendMessage: Boolean = false,
    val isPaginationLoading: Boolean = false,
    val paginationError: UiText? = null,
    val endReached: Boolean = false,
    val messageWithOpenMenu: LocalUserMessage? = null,
    val bannerState: BannerState = BannerState(),
    val isChatOptionsOpen: Boolean = false,
    val isNearBottom: Boolean = false,
    val connectionState: ConnectionStateModel = DISCONNECTED
)

sealed interface ChatDetailEvent {
    data object OnChatLeft: ChatDetailEvent
    data object OnNewMessage: ChatDetailEvent
    data class OnError(val error: UiText): ChatDetailEvent
}

sealed interface ChatDetailAction {
    data object OnSendMessageClick: ChatDetailAction
    data object OnScrollToTop: ChatDetailAction
    data class OnSelectChat(val chatId: String?): ChatDetailAction
    data class OnDeleteMessageClick(val message: LocalUserMessage): ChatDetailAction
    data class OnMessageLongClick(val message: LocalUserMessage): ChatDetailAction
    data object OnDismissMessageMenu: ChatDetailAction
    data class OnRetryClick(val message: LocalUserMessage): ChatDetailAction
    data object OnBackClick: ChatDetailAction
    data object OnChatOptionsClick: ChatDetailAction
    data object OnChatMembersClick: ChatDetailAction
    data object OnLeaveChatClick: ChatDetailAction
    data object OnDismissChatOptions: ChatDetailAction
    data object OnRetryPaginationClick: ChatDetailAction
    data object OnHideBanner: ChatDetailAction
    data class OnFirstVisibleIndexChanged(val index: Int): ChatDetailAction
    data class OnTopVisibleIndexChanged(val topVisibleIndex: Int): ChatDetailAction
}