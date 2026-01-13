package com.kikepb.chat.domain.models

data class ChatParticipantModel(
    val userId: String,
    val username: String,
    val profilePictureUrl: String?
) {
    val initials: String
        get() = username.take(n = 2).uppercase()
}
