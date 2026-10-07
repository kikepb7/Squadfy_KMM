package com.kikepb.chat.presentation.fake

import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.models.ConnectionStateModel
import com.kikepb.chat.domain.models.ConnectionStateModel.DISCONNECTED
import com.kikepb.chat.domain.repository.chat.ChatConnectionClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeChatConnectionClient : ChatConnectionClient {

    private val _chatMessages = MutableSharedFlow<ChatMessageModel>()
    private val _connectionState = MutableStateFlow(value = DISCONNECTED)

    override val chatMessages: Flow<ChatMessageModel> = _chatMessages
    override val connectionState: StateFlow<ConnectionStateModel> = _connectionState

    fun emitConnectionState(state: ConnectionStateModel) {
        _connectionState.value = state
    }
}
