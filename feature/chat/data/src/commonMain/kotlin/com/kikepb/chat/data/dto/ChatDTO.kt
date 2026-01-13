package com.kikepb.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatDTO(
    val id: String,
    val participants: List<ChatParticipantDTO>,
    val lastActivityAt: String,
    val lastMessage: ChatMessageDTO?
)