package com.kikepb.club.domain.policy

import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.ClubMemberRole.ADMIN
import com.kikepb.club.domain.model.ClubMemberRole.CAPTAIN
import com.kikepb.club.domain.model.ClubMemberRole.OWNER
import com.kikepb.club.domain.model.ClubMemberRole.PLAYER
import com.kikepb.club.domain.model.PlayerPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemberPermissionsTest {

    @Test
    fun `AC-003-06 remove and ban follow the role hierarchy (BE-001 RN-9)`() {
        // actor → targets it may act on
        val expected = mapOf(
            OWNER to setOf(ADMIN, CAPTAIN, PLAYER),
            ADMIN to setOf(CAPTAIN, PLAYER),
            CAPTAIN to emptySet(),
            PLAYER to emptySet()
        )
        ClubMemberRole.entries.forEach { actor ->
            ClubMemberRole.entries.forEach { target ->
                val allowed = target in expected.getValue(actor)
                assertEquals(allowed, MemberPermissions.canRemove(actor, target, isSelf = false), "remove $actor → $target")
                assertEquals(allowed, MemberPermissions.canBan(actor, target, isSelf = false), "ban $actor → $target")
            }
        }
    }

    @Test
    fun `AC-003-06 nobody acts on themselves`() {
        ClubMemberRole.entries.forEach { role ->
            assertFalse(MemberPermissions.canRemove(role, role, isSelf = true), "remove self as $role")
            assertTrue(MemberPermissions.assignableRoles(role, role, isSelf = true).isEmpty(), "change own role as $role")
            assertFalse(MemberPermissions.canTransferOwnership(role, isSelf = true), "transfer to self as $role")
        }
    }

    @Test
    fun `AC-003-06 assignable roles per actor exclude OWNER and the current role`() {
        assertEquals(listOf(ADMIN, CAPTAIN), MemberPermissions.assignableRoles(OWNER, PLAYER, isSelf = false))
        assertEquals(listOf(CAPTAIN, PLAYER), MemberPermissions.assignableRoles(OWNER, ADMIN, isSelf = false))
        assertEquals(listOf(PLAYER), MemberPermissions.assignableRoles(ADMIN, CAPTAIN, isSelf = false))
        assertEquals(listOf(CAPTAIN), MemberPermissions.assignableRoles(ADMIN, PLAYER, isSelf = false))
        assertTrue(MemberPermissions.assignableRoles(ADMIN, ADMIN, isSelf = false).isEmpty())
        assertTrue(MemberPermissions.assignableRoles(CAPTAIN, PLAYER, isSelf = false).isEmpty())
    }

    @Test
    fun `AC-003-10 management, bans, leaving and transfer depend on the role`() {
        assertTrue(MemberPermissions.canManageClub(OWNER))
        assertTrue(MemberPermissions.canManageClub(ADMIN))
        assertFalse(MemberPermissions.canManageClub(CAPTAIN))
        assertFalse(MemberPermissions.canSeeBans(PLAYER))
        assertFalse(MemberPermissions.canLeave(OWNER))
        assertTrue(MemberPermissions.canLeave(ADMIN))
        assertTrue(MemberPermissions.canTransferOwnership(OWNER, isSelf = false))
        assertFalse(MemberPermissions.canTransferOwnership(ADMIN, isSelf = false))
    }

    @Test
    fun `AC-003-05 unknown roles and positions are mapped safely (APP-RN-13)`() {
        assertEquals(ADMIN, ClubMemberRole.fromRaw("admin"))
        assertEquals(PLAYER, ClubMemberRole.fromRaw("MEMBER"))
        assertEquals(PLAYER, ClubMemberRole.fromRaw(null))
        assertEquals(PlayerPosition.GOALKEEPER, PlayerPosition.fromRaw("GOALKEEPER"))
        assertNull(PlayerPosition.fromRaw("delantero centro"))
        assertNull(PlayerPosition.fromRaw(null))
    }
}
