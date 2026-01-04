package com.kikepb.chat.domain.repository.chat

import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.models.ConnectionStateModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatConnectionClient {
    val chatMessages: Flow<ChatMessageModel>
    val connectionState: StateFlow<ConnectionStateModel>
}