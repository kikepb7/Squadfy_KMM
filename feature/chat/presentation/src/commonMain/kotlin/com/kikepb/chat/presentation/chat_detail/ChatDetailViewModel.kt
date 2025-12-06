package com.kikepb.chat.presentation.chat_detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.chat.domain.models.ConnectionStateModel
import com.kikepb.chat.domain.models.ConnectionStateModel.DISCONNECTED
import com.kikepb.chat.presentation.model.ChatModelUi
import com.kikepb.chat.presentation.model.MessageModelUi
import com.kikepb.chat.presentation.model.MessageModelUi.LocalUserMessage
import com.kikepb.core.presentation.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class ChatDetailViewModel : ViewModel() {
    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ChatDetailState())
    val state = _state
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

    fun onAction(action: ChatDetailAction) {
        when (action) {
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