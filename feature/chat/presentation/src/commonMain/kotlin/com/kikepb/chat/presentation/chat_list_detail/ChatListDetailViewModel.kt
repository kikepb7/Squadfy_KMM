package com.kikepb.chat.presentation.chat_list_detail

import androidx.lifecycle.ViewModel
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAction.OnChatClick
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAction.OnCreateChatClick
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAction.OnDismissCurrentDialog
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAction.OnManageChatClick
import com.kikepb.chat.presentation.chat_list_detail.ChatListDetailAction.OnProfileSettingsClick
import com.kikepb.chat.presentation.chat_list_detail.DialogState.CreateChat
import com.kikepb.chat.presentation.chat_list_detail.DialogState.Hidden
import com.kikepb.chat.presentation.chat_list_detail.DialogState.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChatListDetailViewModel : ViewModel() {

    private val _state = MutableStateFlow(value = ChatListDetailState())
    val state = _state.asStateFlow()

    fun onAction(action: ChatListDetailAction) {
        when (action) {
            is OnChatClick -> {
                _state.update { it.copy(selectedChatId = action.chatId) }
            }
            OnCreateChatClick -> {
                _state.update { it.copy(dialogState = CreateChat) }
            }
            OnDismissCurrentDialog -> {
                _state.update { it.copy(dialogState = Hidden) }
            }
            OnManageChatClick -> {
                state.value.selectedChatId?.let { chatId ->
                    _state.update { it.copy(
                        dialogState = DialogState.ManageChat(chatId = chatId)
                    ) }
                }
            }
            OnProfileSettingsClick -> {
                _state.update { it.copy(dialogState = Profile) }
            }
        }
    }
}

data class ChatListDetailState(
    val selectedChatId: String? = null,
    val dialogState: DialogState = Hidden
)

sealed interface DialogState {
    data object Hidden: DialogState
    data object CreateChat: DialogState
    data object Profile: DialogState
    data class ManageChat(val chatId: String): DialogState
}

sealed interface ChatListDetailAction {
    data class OnChatClick(val chatId: String?): ChatListDetailAction
    data object OnProfileSettingsClick: ChatListDetailAction
    data object OnCreateChatClick: ChatListDetailAction
    data object OnManageChatClick: ChatListDetailAction
    data object OnDismissCurrentDialog: ChatListDetailAction
}