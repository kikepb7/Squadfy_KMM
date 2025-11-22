package com.kikepb.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatParticipantDTO(
    val userId: String,
    val username: String,
    val profilePictureUrl: String?
)
