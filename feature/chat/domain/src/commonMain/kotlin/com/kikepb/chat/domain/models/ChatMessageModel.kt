package com.kikepb.chat.domain.models

import kotlin.time.Instant

data class ChatMessageModel(
    val id: String,
    val chatId: String,
    val content: String,
    val createdAt: Instant,
    val senderId: String
)
