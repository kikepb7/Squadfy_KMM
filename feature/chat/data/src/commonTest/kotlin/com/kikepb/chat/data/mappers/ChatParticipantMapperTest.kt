package com.kikepb.chat.data.mappers

import com.kikepb.chat.data.dto.ChatParticipantDTO
import com.kikepb.chat.database.entities.ChatParticipantEntity
import com.kikepb.chat.domain.models.ChatParticipantModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChatParticipantMapperTest {

    // --- DTO → Domain ---

    @Test
    fun `GIVEN dto with all fields WHEN toDomain THEN maps correctly`() {
        val dto = ChatParticipantDTO(
            userId = "user-1",
            username = "testuser",
            profilePictureUrl = "https://example.com/pic.jpg"
        )

        val result = dto.toDomain()

        assertEquals("user-1", result.userId)
        assertEquals("testuser", result.username)
        assertEquals("https://example.com/pic.jpg", result.profilePictureUrl)
    }

    @Test
    fun `GIVEN dto with null profilePictureUrl WHEN toDomain THEN profilePictureUrl is null`() {
        val dto = ChatParticipantDTO(
            userId = "user-1",
            username = "testuser",
            profilePictureUrl = null
        )

        val result = dto.toDomain()

        assertNull(result.profilePictureUrl)
    }

    // --- Entity → Domain ---

    @Test
    fun `GIVEN entity WHEN toDomain THEN maps correctly`() {
        val entity = ChatParticipantEntity(
            userId = "user-1",
            username = "testuser",
            profilePictureUrl = "https://example.com/pic.jpg"
        )

        val result = entity.toDomain()

        assertEquals("user-1", result.userId)
        assertEquals("testuser", result.username)
        assertEquals("https://example.com/pic.jpg", result.profilePictureUrl)
    }

    // --- Domain → Entity ---

    @Test
    fun `GIVEN domain model WHEN toEntity THEN maps correctly`() {
        val model = ChatParticipantModel(
            userId = "user-1",
            username = "testuser",
            profilePictureUrl = null
        )

        val result = model.toEntity()

        assertEquals("user-1", result.userId)
        assertEquals("testuser", result.username)
        assertNull(result.profilePictureUrl)
    }

    // --- Initials ---

    @Test
    fun `GIVEN username with 4 chars WHEN initials accessed THEN returns first 2 chars uppercased`() {
        val model = ChatParticipantModel(
            userId = "u1",
            username = "john",
            profilePictureUrl = null
        )

        assertEquals("JO", model.initials)
    }

    @Test
    fun `GIVEN username with 1 char WHEN initials accessed THEN returns single char uppercased`() {
        val model = ChatParticipantModel(
            userId = "u1",
            username = "a",
            profilePictureUrl = null
        )

        assertEquals("A", model.initials)
    }
}
