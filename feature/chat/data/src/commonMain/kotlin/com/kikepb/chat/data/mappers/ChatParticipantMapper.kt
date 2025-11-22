package com.kikepb.chat.data.mappers

import com.kikepb.chat.data.dto.ChatParticipantDTO
import com.kikepb.chat.domain.models.ChatParticipantModel

fun ChatParticipantDTO.toDomain(): ChatParticipantModel {
    return ChatParticipantModel(
        userId = userId,
        username = username,
        profilePictureUrl = profilePictureUrl
    )
}