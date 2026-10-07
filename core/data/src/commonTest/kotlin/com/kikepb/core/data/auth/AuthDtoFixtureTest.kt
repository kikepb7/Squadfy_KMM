package com.kikepb.core.data.auth

import com.kikepb.core.data.auth.dto.AuthInfoSerializableDTO
import com.kikepb.core.data.auth.dto.PublicUserSerializableDTO
import com.kikepb.core.data.mappers.toDomain
import com.kikepb.core.data.networking.squadfyJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Payloads shaped exactly like `Squadfy_Backend/docs/BACKEND.md` §9 (constitution V.4). */
class AuthDtoFixtureTest {

    private val authenticatedUserV1 = """
        {
          "user": {"id":"2c7a1d1e-1111-4a8b-9c3d-000000000001","email":"me@squadfy.app","username":"kike","hasVerifiedEmail":true},
          "accessToken":"eyJhbGciOiJIUzI1NiJ9.access",
          "refreshToken":"eyJhbGciOiJIUzI1NiJ9.refresh"
        }
    """.trimIndent()

    private val publicUserV1 = """
        {"userId":"2c7a1d1e-1111-4a8b-9c3d-000000000001","username":"kike","profilePictureUrl":null}
    """.trimIndent()

    @Test
    fun `AC-002-11 v1 AuthenticatedUserDto without profile picture decodes`() {
        val dto = squadfyJson.decodeFromString<AuthInfoSerializableDTO>(authenticatedUserV1)
        val domain = dto.toDomain()

        assertEquals("kike", domain.user.username)
        assertEquals("eyJhbGciOiJIUzI1NiJ9.refresh", domain.refreshToken)
        assertNull(domain.user.profilePictureUrl)
    }

    @Test
    fun `AC-002-14 v1 public profile decodes with explicit null picture`() {
        val dto = squadfyJson.decodeFromString<PublicUserSerializableDTO>(publicUserV1)

        assertEquals("kike", dto.username)
        assertNull(dto.profilePictureUrl)
    }
}
