package com.kikepb.chat.datafirebase.dto

import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.TimestampSerializer
import kotlinx.serialization.Serializable

/** `chats/{chatId}` (`firebase/functions/src/chat/chat.ts`). */
@Serializable
internal data class ChatDocDto(
    val participantIds: List<String> = emptyList(),
    val participants: Map<String, ParticipantDto> = emptyMap(),
    val lastMessage: LastMessageDto? = null,
    @Serializable(with = TimestampSerializer::class)
    val updatedAt: Timestamp? = null
)

@Serializable
internal data class ParticipantDto(val username: String = "", val pictureUrl: String? = null)

@Serializable
internal data class LastMessageDto(
    val id: String = "",
    val senderId: String = "",
    val content: String = "",
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Timestamp? = null
)

/** `chats/{chatId}/messages/{messageId}`; `createdAt` is the server time (null while pending without an estimate). */
@Serializable
internal data class MessageDto(
    val senderId: String = "",
    val content: String = "",
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Timestamp? = null
)

/** What the app writes: the rules require exactly these fields and the server time. */
@Serializable
internal data class NewMessageDto(
    val senderId: String,
    val content: String,
    val createdAt: dev.gitlive.firebase.firestore.BaseTimestamp
)

@Serializable
internal data class UserDto(val username: String = "", val usernameLower: String = "", val pictureUrl: String? = null)

@Serializable
internal data class DeviceDto(
    val platform: String,
    val locale: String,
    val timeZone: String,
    val appVersion: String,
    val updatedAt: dev.gitlive.firebase.firestore.BaseTimestamp
)

@Serializable
internal data class PictureUpdateDto(
    val pictureUrl: String?,
    val updatedAt: dev.gitlive.firebase.firestore.BaseTimestamp
)

@Serializable internal data class CreateChatRequest(val otherUserIds: List<String>)
@Serializable internal data class ChatIdRequest(val chatId: String)
@Serializable internal data class AddParticipantsRequest(val chatId: String, val userIds: List<String>)
@Serializable internal data class ChatIdResponse(val id: String)
