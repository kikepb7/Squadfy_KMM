package com.kikepb.chat.presentation.create_chat.model

data class ChatParticipantUiModel(
    val id: String,
    val username: String,
    val initials: String,
    val imageUrl: String? = null
)
