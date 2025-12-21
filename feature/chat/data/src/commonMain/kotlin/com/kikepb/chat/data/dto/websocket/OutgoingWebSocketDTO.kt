package com.kikepb.chat.data.dto.websocket

import com.kikepb.chat.data.dto.websocket.OutgoingWebSocketType.NEW_MESSAGE
import kotlinx.serialization.Serializable

enum class OutgoingWebSocketType {
    NEW_MESSAGE
}

@Serializable
sealed class OutgoingWebSocketDTO(
    val type: OutgoingWebSocketType
) {
    @Serializable
    data class NewMessage(
        val chatId: String,
        val messageId: String,
        val content: String
    ): OutgoingWebSocketDTO(type = NEW_MESSAGE)
}