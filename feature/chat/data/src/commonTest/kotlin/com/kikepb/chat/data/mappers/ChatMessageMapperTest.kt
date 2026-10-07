package com.kikepb.chat.data.mappers

import com.kikepb.chat.data.dto.ChatMessageDTO
import com.kikepb.chat.database.entities.ChatMessageEntity
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.models.OutgoingNewMessageModel
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class ChatMessageMapperTest {

    // --- DTO → Domain ---

    @Test
    fun `GIVEN dto WHEN toDomain THEN maps all fields with SENT status`() {
        val dto = ChatMessageDTO(
            id = "msg-1",
            chatId = "chat-1",
            content = "Hello!",
            createdAt = "2024-01-01T00:00:00Z",
            senderId = "user-1"
        )

        val result = dto.toDomain()

        assertEquals("msg-1", result.id)
        assertEquals("chat-1", result.chatId)
        assertEquals("Hello!", result.content)
        assertEquals("user-1", result.senderId)
        assertEquals(ChatMessageDeliveryStatus.SENT, result.deliveryStatus)
    }

    @Test
    fun `GIVEN dto with valid ISO createdAt WHEN toDomain THEN parses timestamp correctly`() {
        val dto = ChatMessageDTO(
            id = "msg-1",
            chatId = "chat-1",
            content = "Hi",
            createdAt = "2024-06-15T10:30:00Z",
            senderId = "user-1"
        )

        val result = dto.toDomain()

        val expected = Instant.parse("2024-06-15T10:30:00Z")
        assertEquals(expected, result.createdAt)
    }

    // --- Entity → Domain ---

    @Test
    fun `GIVEN entity with SENT status WHEN toDomain THEN maps correctly`() {
        val entity = ChatMessageEntity(
            messageId = "msg-1",
            chatId = "chat-1",
            senderId = "user-1",
            content = "Hello!",
            timestamp = 1_700_000_000_000L,
            deliveryStatus = "SENT"
        )

        val result = entity.toDomain()

        assertEquals("msg-1", result.id)
        assertEquals("chat-1", result.chatId)
        assertEquals("Hello!", result.content)
        assertEquals("user-1", result.senderId)
        assertEquals(ChatMessageDeliveryStatus.SENT, result.deliveryStatus)
        assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_000L), result.createdAt)
    }

    @Test
    fun `GIVEN entity with FAILED status WHEN toDomain THEN deliveryStatus is FAILED`() {
        val entity = ChatMessageEntity(
            messageId = "msg-2",
            chatId = "chat-1",
            senderId = "user-1",
            content = "Oops",
            timestamp = 1_700_000_000_000L,
            deliveryStatus = "FAILED"
        )

        val result = entity.toDomain()

        assertEquals(ChatMessageDeliveryStatus.FAILED, result.deliveryStatus)
    }

    // --- Domain → Entity ---

    @Test
    fun `GIVEN domain model WHEN toEntity THEN maps correctly`() {
        val model = ChatMessageModel(
            id = "msg-1",
            chatId = "chat-1",
            content = "Hello!",
            createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000L),
            senderId = "user-1",
            deliveryStatus = ChatMessageDeliveryStatus.SENDING
        )

        val result = model.toEntity()

        assertEquals("msg-1", result.messageId)
        assertEquals("chat-1", result.chatId)
        assertEquals("Hello!", result.content)
        assertEquals("user-1", result.senderId)
        assertEquals("SENDING", result.deliveryStatus)
        assertEquals(1_700_000_000_000L, result.timestamp)
    }

    // --- OutgoingNewMessageModel → WebSocket DTO ---

    @Test
    fun `GIVEN outgoing message WHEN toWebSocketDto THEN maps correctly`() {
        val model = OutgoingNewMessageModel(
            chatId = "chat-1",
            messageId = "msg-1",
            content = "Hello!"
        )

        val result = model.toWebSocketDto()

        assertEquals("chat-1", result.chatId)
        assertEquals("msg-1", result.messageId)
        assertEquals("Hello!", result.content)
    }
}
