package com.kikepb.chat.domain.repository

import com.kikepb.chat.domain.error.ConnectionErrorModel
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.models.ConnectionStateModel
import com.kikepb.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatConnectionClient {
    val chatMessages: Flow<ChatMessageModel>
    val connectionState: StateFlow<ConnectionStateModel>
    suspend fun sendChatMessage(message: ChatMessageModel): EmptyResult<ConnectionErrorModel>
}