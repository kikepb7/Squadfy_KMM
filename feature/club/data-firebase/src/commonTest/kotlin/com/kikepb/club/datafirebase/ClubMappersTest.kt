package com.kikepb.club.datafirebase

import com.kikepb.club.datafirebase.dto.ClubDocDto
import com.kikepb.club.datafirebase.dto.ClubResponseDto
import com.kikepb.club.datafirebase.dto.MemberDocDto
import com.kikepb.club.datafirebase.dto.isActive
import com.kikepb.club.datafirebase.dto.toDomain
import com.kikepb.club.datafirebase.util.MemberIds
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.PlayerPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class ClubMappersTest {

    @Test
    fun `AC-019-07 a club document maps to the same model as the own backend ClubDto`() {
        val club = ClubDocDto(name = "Churriana FC", logoUrl = "https://x/logo.jpg", ownerId = "u1", invitationCode = "K7Q2M9AB", maxMembers = 20, membersCount = 14)
            .toDomain("c1")
        assertEquals("c1", club.id)
        assertEquals("https://x/logo.jpg", club.clubLogoUrl)
        assertEquals(14, club.membersCount)
        assertEquals(club, ClubResponseDto(id = "c1", name = "Churriana FC", clubLogoUrl = "https://x/logo.jpg", ownerId = "u1", invitationCode = "K7Q2M9AB", maxMembers = 20, membersCount = 14).toDomain())
    }

    @Test
    fun `AC-019-07 members map their role and position with the app fallbacks`() {
        val member = MemberDocDto(clubId = "c1", userId = "u1", username = "Álvaro", position = "MIDFIELDER", role = "ADMIN", clubPictureUrl = "https://x/me.jpg")
            .toDomain("c1_u1")
        assertEquals("c1_u1", member.id)
        assertEquals(ClubMemberRole.ADMIN, member.role)
        assertEquals(PlayerPosition.MIDFIELDER, member.position)
        assertEquals("https://x/me.jpg", member.pictureUrl)
        assertEquals(ClubMemberRole.PLAYER, MemberDocDto(role = "MEMBER").toDomain("x").role)
        assertNull(MemberDocDto(position = "striker").toDomain("x").position)
        assertFalse(MemberDocDto(status = "BANNED").isActive)
    }

    @Test
    fun `AC-019-07 membership ids are clubId_uid`() {
        assertEquals("c1_u1", MemberIds.of("c1", "u1"))
        assertEquals("c1", MemberIds.clubOf("c1_u1"))
        assertNull(MemberIds.clubOf("legacy-uuid"))
    }
}
