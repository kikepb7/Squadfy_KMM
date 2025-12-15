package com.kikepb.chat.data.mappers

import com.kikepb.chat.data.dto.ChatDTO
import com.kikepb.chat.database.entities.ChatEntity
import com.kikepb.chat.database.entities.ChatInfoEntity
import com.kikepb.chat.database.entities.ChatWithParticipants
import com.kikepb.chat.database.entities.MessageWithSender
import com.kikepb.chat.domain.models.ChatInfoModel
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.domain.models.MessageWithSenderModel
import kotlin.time.Instant

typealias DataMessageWithSender = MessageWithSender
typealias DomainMessageWithSender =  MessageWithSenderModel
fun ChatDTO.toDomain(): ChatModel =
    ChatModel(
        id = id,
        participants = participants.map { it.toDomain() },
        lastActivityAt = Instant.parse(input = lastActivityAt),
        lastMessage = lastMessage?.toDomain()
    )

fun ChatEntity.toDomain(
    participants: List<ChatParticipantModel>,
    lastMessage: ChatMessageModel? = null
): ChatModel =
    ChatModel(
        id = chatId,
        participants = participants,
        lastActivityAt = Instant.fromEpochMilliseconds(epochMilliseconds = lastActivityAt),
        lastMessage = lastMessage
    )

fun ChatWithParticipants.toDomain(): ChatModel =
    ChatModel(
        id = chat.chatId,
        participants = participants.map { it.toDomain() },
        lastActivityAt = Instant.fromEpochMilliseconds(epochMilliseconds = chat.lastActivityAt),
        lastMessage = lastMessage?.toDomain()
    )

fun ChatModel.toEntity(): ChatEntity =
    ChatEntity(
        chatId = id,
        lastActivityAt = lastActivityAt.toEpochMilliseconds()
    )

fun DataMessageWithSender.toDomain(): DomainMessageWithSender =
    DomainMessageWithSender(
        message = message.toDomain(),
        sender = sender.toDomain(),
        deliveryStatus = ChatMessageDeliveryStatus.valueOf(value = this.message.deliveryStatus)
    )

fun ChatInfoEntity.toDomain(): ChatInfoModel =
    ChatInfoModel(
        chat = chat.toDomain(
            participants = this.participants.map { it.toDomain() }
        ),
        messages = messagesWithSenders.map { it.toDomain() }
    )