package com.kikepb.chat.data.mappers

import com.kikepb.chat.data.dto.ChatDTO
import com.kikepb.chat.data.dto.ChatMessageDTO
import com.kikepb.chat.data.dto.ChatParticipantDTO
import com.kikepb.chat.database.entities.ChatEntity
import com.kikepb.chat.database.entities.ChatMessageEntity
import com.kikepb.chat.database.entities.ChatParticipantEntity
import com.kikepb.chat.database.entities.ChatWithParticipants
import com.kikepb.chat.database.entities.MessageWithSender
import com.kikepb.chat.database.view.LastMessageView
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.models.ChatParticipantModel
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChatMapperTest {

    // --- ChatDTO → Domain ---

    @Test
    fun `GIVEN dto without last message WHEN toDomain THEN lastMessage is null`() {
        val dto = ChatDTO(
            id = "chat-1",
            participants = listOf(
                ChatParticipantDTO(userId = "u1", username = "alice", profilePictureUrl = null)
            ),
            lastActivityAt = "2024-01-01T00:00:00Z",
            lastMessage = null
        )

        val result = dto.toDomain()

        assertEquals("chat-1", result.id)
        assertEquals(1, result.participants.size)
        assertNull(result.lastMessage)
        assertNull(result.lastMessageSenderUsername)
    }

    @Test
    fun `GIVEN dto with last message WHEN toDomain THEN resolves sender username from participants`() {
        val dto = ChatDTO(
            id = "chat-1",
            participants = listOf(
                ChatParticipantDTO(userId = "u1", username = "alice", profilePictureUrl = null),
                ChatParticipantDTO(userId = "u2", username = "bob", profilePictureUrl = null)
            ),
            lastActivityAt = "2024-01-01T00:00:00Z",
            lastMessage = ChatMessageDTO(
                id = "msg-1",
                chatId = "chat-1",
                content = "Hi!",
                createdAt = "2024-01-01T00:00:00Z",
                senderId = "u2"
            )
        )

        val result = dto.toDomain()

        assertEquals("bob", result.lastMessageSenderUsername)
        assertEquals("Hi!", result.lastMessage?.content)
    }

    @Test
    fun `GIVEN dto with last message from unknown sender WHEN toDomain THEN lastMessageSenderUsername is null`() {
        val dto = ChatDTO(
            id = "chat-1",
            participants = listOf(
                ChatParticipantDTO(userId = "u1", username = "alice", profilePictureUrl = null)
            ),
            lastActivityAt = "2024-01-01T00:00:00Z",
            lastMessage = ChatMessageDTO(
                id = "msg-1",
                chatId = "chat-1",
                content = "Hi!",
                createdAt = "2024-01-01T00:00:00Z",
                senderId = "unknown-sender"
            )
        )

        val result = dto.toDomain()

        assertNull(result.lastMessageSenderUsername)
    }

    // --- ChatModel → Entity ---

    @Test
    fun `GIVEN domain model WHEN toEntity THEN maps id and timestamp`() {
        val model = com.kikepb.chat.domain.models.ChatModel(
            id = "chat-1",
            participants = emptyList(),
            lastActivityAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
            lastMessage = null
        )

        val result = model.toEntity()

        assertEquals("chat-1", result.chatId)
        assertEquals(1_700_000_000_000L, result.lastActivityAt)
    }

    // --- ChatWithParticipants → Domain ---

    @Test
    fun `GIVEN ChatWithParticipants without last message WHEN toDomain THEN lastMessage is null`() {
        val chatEntity = ChatEntity(chatId = "chat-1", lastActivityAt = 1_700_000_000_000L)
        val participants = listOf(
            ChatParticipantEntity(userId = "u1", username = "alice", profilePictureUrl = null)
        )
        val withParticipants = ChatWithParticipants(
            chat = chatEntity,
            participants = participants,
            lastMessage = null
        )

        val result = withParticipants.toDomain()

        assertEquals("chat-1", result.id)
        assertEquals(1, result.participants.size)
        assertNull(result.lastMessage)
    }

    @Test
    fun `GIVEN ChatWithParticipants with last message WHEN toDomain THEN maps senderUsername`() {
        val chatEntity = ChatEntity(chatId = "chat-1", lastActivityAt = 1_700_000_000_000L)
        val participants = listOf(
            ChatParticipantEntity(userId = "u1", username = "alice", profilePictureUrl = null)
        )
        val lastMessage = LastMessageView(
            messageId = "msg-1",
            chatId = "chat-1",
            senderId = "u1",
            content = "Hey!",
            timestamp = 1_700_000_000_000L,
            deliveryStatus = "SENT",
            senderUsername = "alice"
        )
        val withParticipants = ChatWithParticipants(
            chat = chatEntity,
            participants = participants,
            lastMessage = lastMessage
        )

        val result = withParticipants.toDomain()

        assertEquals("alice", result.lastMessageSenderUsername)
        assertEquals("Hey!", result.lastMessage?.content)
    }

    // --- MessageWithSender → Domain ---

    @Test
    fun `GIVEN MessageWithSender WHEN toDomain THEN maps message and sender correctly`() {
        val messageEntity = ChatMessageEntity(
            messageId = "msg-1",
            chatId = "chat-1",
            senderId = "u1",
            content = "Hi!",
            timestamp = 1_700_000_000_000L,
            deliveryStatus = "SENT"
        )
        val senderEntity = ChatParticipantEntity(
            userId = "u1",
            username = "alice",
            profilePictureUrl = null
        )
        val messageWithSender = MessageWithSender(message = messageEntity, sender = senderEntity)

        val result = messageWithSender.toDomain()

        assertEquals("msg-1", result.message.id)
        assertEquals("alice", result.sender.username)
        assertEquals(ChatMessageDeliveryStatus.SENT, result.deliveryStatus)
    }
}
