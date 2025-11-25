package com.kikepb.chat.presentation.model

import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi

data class ChatModelUi(
    val id: String,
    val localParticipant: ChatParticipantModelUi,
    val otherParticipants: List<ChatParticipantModelUi>,
    val lastMessage: ChatMessageModel?,
    val lastMessageSenderUsername: String?
)