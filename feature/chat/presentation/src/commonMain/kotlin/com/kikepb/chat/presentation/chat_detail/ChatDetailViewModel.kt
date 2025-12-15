@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kikepb.chat.presentation.chat_detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.chat.domain.models.ConnectionStateModel
import com.kikepb.chat.domain.models.ConnectionStateModel.DISCONNECTED
import com.kikepb.chat.domain.usecases.FetchChatByIdUseCase
import com.kikepb.chat.domain.usecases.GetChatInfoByIdUseCase
import com.kikepb.chat.presentation.mappers.toUi
import com.kikepb.chat.presentation.model.ChatModelUi
import com.kikepb.chat.presentation.model.MessageModelUi
import com.kikepb.chat.presentation.model.MessageModelUi.LocalUserMessage
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatDetailViewModel(
    private val fetchChatByIdUseCase: FetchChatByIdUseCase,
    private val getChatInfoByIdUseCase: GetChatInfoByIdUseCase,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _chatId = MutableStateFlow<String?>(value = null)
    private var hasLoadedInitialData = false
    private val chatInfoFlow = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) getChatInfoByIdUseCase.getChatInfoById(chatId = chatId) else emptyFlow()
        }

    private val _state = MutableStateFlow(ChatDetailState())
    private val stateWithMessages = combine(
        _state,
        chatInfoFlow,
        sessionStorage.observeAuthInfo()
    ) { currentState, chatInfo, authInfo ->
        if (authInfo == null) return@combine ChatDetailState()

        currentState.copy(
            chatUi = chatInfo.chat.toUi(localParticipantId = authInfo.user.id)
        )
    }
    val state = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) stateWithMessages else _state
        }
        .onStart {
            if (!hasLoadedInitialData) {

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

    fun onAction(action: ChatDetailAction) {
        when (action) {
            is ChatDetailAction.OnSelectChat -> switchChat(chatId = action.chatId)
            else -> TODO("Handle actions")
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