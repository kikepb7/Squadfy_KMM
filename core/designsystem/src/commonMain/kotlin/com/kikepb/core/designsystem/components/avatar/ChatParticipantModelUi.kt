package com.kikepb.core.designsystem.components.avatar

data class ChatParticipantModelUi(
    val id: String,
    val username: String,
    val initials: String,
    val imageUrl: String? = null
)
