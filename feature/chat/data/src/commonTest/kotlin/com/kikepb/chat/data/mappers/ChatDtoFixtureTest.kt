package com.kikepb.chat.data.mappers

import com.kikepb.chat.data.dto.ChatDTO
import com.kikepb.core.data.networking.squadfyJson
import kotlin.test.Test
import kotlin.test.assertEquals

/** `ChatDto` exactly as `Squadfy_Backend/docs/BACKEND.md` §9 describes it (v1 adds `creator`). */
class ChatDtoFixtureTest {

    private val chatV1 = """
        {
          "id":"chat-1",
          "participants":[
            {"userId":"u-1","username":"kike","profilePictureUrl":null},
            {"userId":"u-2","username":"ana","profilePictureUrl":"https://cdn/ana.jpg"}
          ],
          "lastActivityAt":"2026-10-07T00:13:51.413967Z",
          "lastMessage":{"id":"m-1","chatId":"chat-1","content":"Hola","createdAt":"2026-10-07T00:13:51.413967Z","senderId":"u-2"},
          "creator":{"userId":"u-1","username":"kike","profilePictureUrl":null}
        }
    """.trimIndent()

    @Test
    fun `AC-002-15 v1 ChatDto with creator and microsecond instants decodes and maps`() {
        val dto = squadfyJson.decodeFromString<ChatDTO>(chatV1)
        val chat = dto.toDomain()

        assertEquals("u-1", dto.creator?.userId)
        assertEquals(2, chat.participants.size)
        assertEquals("Hola", chat.lastMessage?.content)
    }
}
