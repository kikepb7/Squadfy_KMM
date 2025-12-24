package com.kikepb.chat.data.dto.websocket

import com.kikepb.chat.data.dto.websocket.OutgoingWebSocketType.NEW_MESSAGE
import kotlinx.serialization.Serializable

enum class OutgoingWebSocketType {
    NEW_MESSAGE
}

@Serializable
sealed interface OutgoingWebSocketDTO {
    @Serializable
    data class NewMessage(
        val chatId: String,
        val messageId: String,
        val content: String,
        val type: OutgoingWebSocketType = NEW_MESSAGE
    ): OutgoingWebSocketDTO
}