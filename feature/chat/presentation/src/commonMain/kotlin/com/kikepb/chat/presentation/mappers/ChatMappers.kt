package com.kikepb.chat.presentation.mappers

import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.chat.presentation.model.ChatModelUi

fun ChatModel.toUi(localParticipantId: String): ChatModelUi {
    val (local, other) = participants.partition { it.userId == localParticipantId }

    return ChatModelUi(
        id = id,
        localParticipant = local.first().toUi(),
        otherParticipants = other.map { it.toUi() },
        lastMessage = lastMessage,
        lastMessageSenderUsername = participants
            .find { it.userId == lastMessage?.senderId }
            ?.username
    )
}