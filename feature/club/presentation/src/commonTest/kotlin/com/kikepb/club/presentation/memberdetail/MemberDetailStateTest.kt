package com.kikepb.club.presentation.memberdetail

import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubMemberRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemberDetailStateTest {

    private fun member(id: String, role: ClubMemberRole) = ClubMemberModel(
        id = id, clubId = "c", userId = "u-$id", username = id, profilePictureUrl = null, shirtNumber = null, position = null, role = role
    )

    @Test
    fun `AC-003-07 an admin looking at a player sees role, remove and ban but not transfer`() {
        val state = MemberDetailState(member = member("p", ClubMemberRole.PLAYER), me = member("a", ClubMemberRole.ADMIN))

        assertEquals(listOf(ClubMemberRole.CAPTAIN), state.assignableRoles)
        assertTrue(state.canRemove)
        assertTrue(state.canBan)
        assertFalse(state.canTransfer)
    }

    @Test
    fun `AC-003-07 an admin looking at another admin has no actions`() {
        val state = MemberDetailState(member = member("x", ClubMemberRole.ADMIN), me = member("a", ClubMemberRole.ADMIN))

        assertTrue(state.assignableRoles.isEmpty())
        assertFalse(state.canRemove)
        assertFalse(state.canBan)
    }

    @Test
    fun `AC-003-08 my own profile only allows editing my membership`() {
        val me = member("o", ClubMemberRole.OWNER)
        val state = MemberDetailState(member = me, me = me)

        assertTrue(state.isSelf)
        assertTrue(state.assignableRoles.isEmpty())
        assertFalse(state.canRemove)
        assertFalse(state.canTransfer)
    }

    @Test
    fun `AC-003-07 the owner can transfer ownership to anyone else`() {
        val state = MemberDetailState(member = member("c", ClubMemberRole.CAPTAIN), me = member("o", ClubMemberRole.OWNER))

        assertTrue(state.canTransfer)
        assertEquals(listOf(ClubMemberRole.ADMIN, ClubMemberRole.PLAYER), state.assignableRoles)
    }
}
