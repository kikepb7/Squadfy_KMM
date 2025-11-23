package com.kikepb.chat.data.mappers

import com.kikepb.chat.data.dto.ChatMessageDTO
import com.kikepb.chat.domain.models.ChatMessageModel
import kotlin.time.Instant

fun ChatMessageDTO.toDomain(): ChatMessageModel =
    ChatMessageModel(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = Instant.parse(input = createdAt),
        senderId = senderId
    )