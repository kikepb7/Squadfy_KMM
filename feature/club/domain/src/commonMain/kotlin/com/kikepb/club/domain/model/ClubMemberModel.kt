package com.kikepb.club.domain.model

/**
 * A user's membership in one club (`ClubMemberDto`). [id] is the `clubMemberId` used by teams,
 * announcements, ratings and stats. Email and stats are not part of v1 members (BE-001, BE-004).
 */
data class ClubMemberModel(
    val id: String,
    val clubId: String,
    val userId: String,
    val username: String,
    val profilePictureUrl: String?,
    val shirtNumber: Int?,
    val position: PlayerPosition?,
    val role: ClubMemberRole
)

/** A banned member (`ClubBanDto`), only visible to managers. */
data class ClubBanModel(
    val clubMemberId: String,
    val userId: String,
    val username: String,
    val bannedAt: String
)
