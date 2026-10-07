package com.kikepb.club.domain.policy

import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.ClubMemberRole.ADMIN
import com.kikepb.club.domain.model.ClubMemberRole.CAPTAIN
import com.kikepb.club.domain.model.ClubMemberRole.OWNER
import com.kikepb.club.domain.model.ClubMemberRole.PLAYER

/**
 * What the current member may do in the UI (APP-RN-04/05), mirroring the backend hierarchy (BE-001 RN-9):
 * - OWNER acts on anyone and assigns ADMIN, CAPTAIN or PLAYER.
 * - ADMIN only acts on CAPTAIN/PLAYER and only toggles between those two roles.
 * - Nobody acts on themselves; OWNER only changes through a transfer.
 * The server stays authoritative: a 403 always wins over what this policy shows.
 */
object MemberPermissions {

    fun canManageClub(actor: ClubMemberRole): Boolean = actor.isManager

    fun canLeave(actor: ClubMemberRole): Boolean = actor != OWNER

    fun canSeeBans(actor: ClubMemberRole): Boolean = actor.isManager

    fun canRemove(actor: ClubMemberRole, target: ClubMemberRole, isSelf: Boolean): Boolean =
        !isSelf && canActOn(actor = actor, target = target)

    fun canBan(actor: ClubMemberRole, target: ClubMemberRole, isSelf: Boolean): Boolean =
        canRemove(actor = actor, target = target, isSelf = isSelf)

    fun assignableRoles(actor: ClubMemberRole, target: ClubMemberRole, isSelf: Boolean): List<ClubMemberRole> {
        if (isSelf || !canActOn(actor = actor, target = target)) return emptyList()
        val roles = when (actor) {
            OWNER -> listOf(ADMIN, CAPTAIN, PLAYER)
            ADMIN -> listOf(CAPTAIN, PLAYER)
            CAPTAIN, PLAYER -> emptyList()
        }
        return roles - target
    }

    fun canTransferOwnership(actor: ClubMemberRole, isSelf: Boolean): Boolean = actor == OWNER && !isSelf

    private fun canActOn(actor: ClubMemberRole, target: ClubMemberRole): Boolean = when (actor) {
        OWNER -> target != OWNER
        ADMIN -> target == CAPTAIN || target == PLAYER
        CAPTAIN, PLAYER -> false
    }
}
