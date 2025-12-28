@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)

package com.kikepb.chat.presentation.chat_detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.chat.domain.models.ConnectionStateModel
import com.kikepb.chat.domain.models.ConnectionStateModel.CONNECTED
import com.kikepb.chat.domain.models.ConnectionStateModel.DISCONNECTED
import com.kikepb.chat.domain.models.OutgoingNewMessageModel
import com.kikepb.chat.domain.repository.ChatConnectionClient
import com.kikepb.chat.domain.usecases.FetchChatByIdUseCase
import com.kikepb.chat.domain.usecases.GetChatInfoByIdUseCase
import com.kikepb.chat.domain.usecases.LeaveChatUseCase
import com.kikepb.chat.domain.usecases.message.FetchMessagesUseCase
import com.kikepb.chat.domain.usecases.message.GetMessagesForChatUseCase
import com.kikepb.chat.domain.usecases.message.RetryMessageUseCase
import com.kikepb.chat.domain.usecases.message.SendMessageUseCase
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnBackClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnChatMembersClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnChatOptionsClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnDeleteMessageClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnDismissChatOptions
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnDismissMessageMenu
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnLeaveChatClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnMessageLongClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnRetryClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnScrollToTop
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnSelectChat
import com.kikepb.chat.presentation.chat_detail.ChatDetailAction.OnSendMessageClick
import com.kikepb.chat.presentation.chat_detail.ChatDetailEvent.OnError
import com.kikepb.chat.presentation.chat_detail.ChatDetailEvent.OnNewMessage
import com.kikepb.chat.presentation.mappers.toUi
import com.kikepb.chat.presentation.model.ChatModelUi
import com.kikepb.chat.presentation.model.MessageModelUi
import com.kikepb.chat.presentation.model.MessageModelUi.LocalUserMessage
import com.kikepb.core.domain.auth.repository.SessionStorage
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
    private val chatConnectionClient: ChatConnectionClient,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val eventChannel = Channel<ChatDetailEvent>()
    val events = eventChannel.receiveAsFlow()
    private val _chatId = MutableStateFlow<String?>(value = null)
    private var hasLoadedInitialData = false
    private val chatInfoFlow = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) getChatInfoByIdUseCase.getChatInfoById(chatId = chatId) else emptyFlow()
        }

    private val _state = MutableStateFlow(ChatDetailState())
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
            messages = chatInfo.messages.map { it.toUi(localUserId = authInfo.user.id) }
        )
    }
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

    private fun switchChat(chatId: String?) {
        _chatId.update { chatId }
        viewModelScope.launch {
            chatId?.let {
                fetchChatByIdUseCase.fetchChatById(chatId = chatId)
            }
        }
    }

    private fun onChatOptionsClick() {
        _state.update { it.copy(isChatOptionsOpen = true) }
    }

    private fun onDismissChatOptions() {
        _state.update { it.copy(isChatOptionsOpen = false) }
    }

    private fun onLeaveChatClick() {
        val chatId = _chatId.value ?: return

        _state.update { it.copy(isChatOptionsOpen = false) }

        viewModelScope.launch {
            leaveChatUseCase.leaveChat(chatId = chatId)
                .onSuccess {
                    _state.value.messageTextFieldState.clearText()

                    _chatId.update { null }
                    _state.update { it.copy(chatUi = null, messages = emptyList(), bannerState = BannerState()) }
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
                if (connectionState == CONNECTED) {
                    _chatId.value?.let {
                        fetchMessagesUseCase.fetchMessages(chatId = it, before = null)
                    }
                }

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
            val lastNewId = newMessages.lastOrNull()?.message?.id
            val lastCurrentId = currentMessages.lastOrNull()?.id

            if (lastNewId != lastCurrentId && isNearBottom) eventChannel.send(OnNewMessage)
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

    fun onAction(action: ChatDetailAction) {
        when (action) {
            is OnSelectChat -> switchChat(chatId = action.chatId)
            OnBackClick -> {}
            OnChatMembersClick -> {}
            OnChatOptionsClick -> onChatOptionsClick()
            is OnDeleteMessageClick -> {}
            OnDismissChatOptions -> onDismissChatOptions()
            OnDismissMessageMenu -> {}
            OnLeaveChatClick -> onLeaveChatClick()
            is OnMessageLongClick -> {}
            is OnRetryClick -> retryMessage(message = action.message)
            OnScrollToTop -> {}
            OnSendMessageClick -> sendMessage()
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

data class BannerState(
    val formattedDate: UiText? = null,
    val isVisible: Boolean = false
)

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
}