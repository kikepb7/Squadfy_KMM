package com.kikepb.chat.data.dto.websocket

import com.kikepb.chat.data.dto.websocket.IncomingWebSocketType.CHAT_PARTICIPANTS_CHANGED
import com.kikepb.chat.data.dto.websocket.IncomingWebSocketType.MESSAGE_DELETED
import com.kikepb.chat.data.dto.websocket.IncomingWebSocketType.NEW_MESSAGE
import com.kikepb.chat.data.dto.websocket.IncomingWebSocketType.PROFILE_PICTURE_UPDATED
import kotlinx.serialization.Serializable

enum class IncomingWebSocketType {
    NEW_MESSAGE,
    MESSAGE_DELETED,
    PROFILE_PICTURE_UPDATED,
    CHAT_PARTICIPANTS_CHANGED
}

@Serializable
sealed class IncomingWebSocketDTO(
    val type: IncomingWebSocketType
) {
    @Serializable
    data class NewMessageDTO(
        val id: String,
        val chatId: String,
        val content: String,
        val senderId: String,
        val createdAt: String
    ): IncomingWebSocketDTO(type = NEW_MESSAGE)

    @Serializable
    data class MessageDeletedDTO(
        val messageId: String,
        val chatId: String
    ): IncomingWebSocketDTO(type = MESSAGE_DELETED)

    @Serializable
    data class ProfilePictureUpdatedDTO(
        val userId: String,
        val newUrl: String?
    ): IncomingWebSocketDTO(type = PROFILE_PICTURE_UPDATED)

    @Serializable
    data class ChatParticipantChangedDTO(
        val chatId: String
    ): IncomingWebSocketDTO(type = CHAT_PARTICIPANTS_CHANGED)
}
