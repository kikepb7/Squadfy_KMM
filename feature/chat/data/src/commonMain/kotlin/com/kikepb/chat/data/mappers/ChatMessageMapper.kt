package com.kikepb.chat.data.mappers

import com.kikepb.chat.data.dto.ChatMessageDTO
import com.kikepb.chat.data.dto.websocket.IncomingWebSocketDTO
import com.kikepb.chat.data.dto.websocket.OutgoingWebSocketDTO
import com.kikepb.chat.database.entities.ChatMessageEntity
import com.kikepb.chat.database.view.LastMessageView
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageModel
import kotlin.time.Instant

fun ChatMessageDTO.toDomain(): ChatMessageModel =
    ChatMessageModel(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = Instant.parse(input = createdAt),
        senderId = senderId,
        deliveryStatus = ChatMessageDeliveryStatus.SENT
    )

fun ChatMessageEntity.toDomain(): ChatMessageModel =
    ChatMessageModel(
        id = chatId,
        chatId = chatId,
        content = content,
        createdAt = Instant.fromEpochMilliseconds(epochMilliseconds = timestamp),
        senderId = senderId,
        deliveryStatus = ChatMessageDeliveryStatus.SENT
    )

fun LastMessageView.toDomain(): ChatMessageModel =
    ChatMessageModel(
        id = messageId,
        chatId = chatId,
        content = content,
        createdAt = Instant.fromEpochMilliseconds(epochMilliseconds = timestamp),
        senderId = senderId,
        deliveryStatus = ChatMessageDeliveryStatus.valueOf(value = this.deliveryStatus)
    )

fun ChatMessageModel.toEntity(): ChatMessageEntity =
    ChatMessageEntity(
        messageId = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        timestamp = createdAt.toEpochMilliseconds(),
        deliveryStatus = deliveryStatus.name
    )

fun ChatMessageModel.toLastMessageView(): LastMessageView =
    LastMessageView(
        messageId = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        timestamp = createdAt.toEpochMilliseconds(),
        deliveryStatus = deliveryStatus.name
    )

fun ChatMessageModel.toNewMessage(): OutgoingWebSocketDTO.NewMessage =
    OutgoingWebSocketDTO.NewMessage(
        messageId = id,
        chatId = chatId,
        content = content
    )
fun IncomingWebSocketDTO.NewMessageDTO.toEntity(): ChatMessageEntity =
    ChatMessageEntity(
        messageId = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        timestamp = Instant.parse(createdAt).toEpochMilliseconds(),
        deliveryStatus = ChatMessageDeliveryStatus.SENT.name
    )