package com.kikepb.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageDTO(
    val id: String,
    val chatId: String,
    val content: String,
    val createdAt: String,
    val senderId: String
)
