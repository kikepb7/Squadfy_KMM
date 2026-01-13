package com.kikepb.chat.domain.models

data class OutgoingNewMessageModel(
    val chatId: String,
    val messageId: String,
    val content: String
)