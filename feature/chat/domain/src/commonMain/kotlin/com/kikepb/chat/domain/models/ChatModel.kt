package com.kikepb.chat.domain.models

import kotlin.time.Instant

data class ChatModel(
    val id: String,
    val participants: List<ChatParticipantModel>,
    val lastActivityAt: Instant,
    val lastMessage: ChatMessageModel?,
    val lastMessageSenderUsername: String? = null
)
