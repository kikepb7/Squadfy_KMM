package com.kikepb.chat.data.mappers

import com.kikepb.chat.data.dto.ChatDTO
import com.kikepb.chat.domain.models.ChatModel
import kotlin.time.Instant

fun ChatDTO.toDomain(): ChatModel =
    ChatModel(
        id = id,
        participants = participants.map { it.toDomain() },
        lastActivityAt = Instant.parse(input = lastActivityAt),
        lastMessage = lastMessage?.toDomain()
    )