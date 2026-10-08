package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.ClubDTO
import com.kikepb.club.data.dto.ClubMemberDTO
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.core.data.networking.squadfyJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Payloads shaped like `Squadfy_Backend/docs/BACKEND.md` §9 (constitution V.4). */
class ClubDtoFixtureTest {

    private val clubV1 = """
        {"id":"c9080e51-2da6-43bb-acb5-c0271b4dd1c2","name":"Squadfy FC","description":null,"clubLogoUrl":null,
         "ownerId":"u-1","invitationCode":"AB12CD34","maxMembers":null,"membersCount":12,
         "createdAt":"2026-10-07T00:13:51.435223Z","updatedAt":"2026-10-07T00:13:51.435225Z"}
    """.trimIndent()

    private val membersV1 = """
        [
          {"id":"m-1","clubId":"c-1","userId":"u-1","username":"kike","profilePictureUrl":null,"shirtNumber":10,
           "position":"FORWARD","role":"OWNER","createdAt":"2026-10-07T00:13:51Z","updatedAt":"2026-10-07T00:13:51Z"},
          {"id":"m-2","clubId":"c-1","userId":"u-2","username":"ana","profilePictureUrl":"https://cdn/ana.jpg","shirtNumber":null,
           "position":"delantero centro","role":"COACH","createdAt":"2026-10-07T00:13:51Z","updatedAt":"2026-10-07T00:13:51Z"}
        ]
    """.trimIndent()

    @Test
    fun `AC-003-05 v1 club decodes and maps without schedule fields`() {
        val club = squadfyJson.decodeFromString<ClubDTO>(clubV1).toDomain()

        assertEquals("Squadfy FC", club.name)
        assertEquals("AB12CD34", club.invitationCode)
        assertEquals(12, club.membersCount)
        assertNull(club.maxMembers)
    }

    @Test
    fun `AC-003-05 v1 members decode without email or stats and enums map safely`() {
        val members = squadfyJson.decodeFromString<List<ClubMemberDTO>>(membersV1).map { it.toDomain() }

        assertEquals(ClubMemberRole.OWNER, members[0].role)
        assertEquals(PlayerPosition.FORWARD, members[0].position)
        assertEquals(10, members[0].shirtNumber)
        // Unknown role and legacy free-text position (APP-RN-13)
        assertEquals(ClubMemberRole.PLAYER, members[1].role)
        assertNull(members[1].position)
    }

    @Test
    fun `AC-003-05 cached member keeps the normalized enum names`() {
        val entity = squadfyJson.decodeFromString<List<ClubMemberDTO>>(membersV1)[1].toEntity()

        assertEquals("PLAYER", entity.role)
        assertNull(entity.position)
    }

    @Test
    fun `AC-015-05 member pictures from backend spec 012 map to the club picture and the one to show`() {
        val json = """
            {"id":"m-1","clubId":"c-1","userId":"u-1","username":"kike","profilePictureUrl":"https://cdn/p.jpg",
             "clubPictureUrl":"https://cdn/c.jpg","pictureUrl":"https://cdn/c.jpg","shirtNumber":7,"position":"FORWARD","role":"OWNER"}
        """.trimIndent()
        val member = squadfyJson.decodeFromString<ClubMemberDTO>(json).toDomain()

        assertEquals("https://cdn/c.jpg", member.clubPictureUrl)
        assertEquals("https://cdn/c.jpg", member.pictureUrl)
        assertEquals("https://cdn/p.jpg", member.profilePictureUrl)

        val withoutClubPicture = squadfyJson.decodeFromString<ClubMemberDTO>(json.replace("\"clubPictureUrl\":\"https://cdn/c.jpg\",", "")).toDomain()
        assertNull(withoutClubPicture.clubPictureUrl)
        assertEquals("https://cdn/p.jpg", withoutClubPicture.pictureUrl)
    }
}
