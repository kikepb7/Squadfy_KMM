package com.kikepb.club.data.dto

import kotlinx.serialization.Serializable

/** `ClubMemberDto` of the backend API v1: no email and no stats (they live in `/stats`). */
@Serializable
data class ClubMemberDTO(
    val id: String,
    val clubId: String,
    val userId: String,
    val username: String,
    val profilePictureUrl: String? = null,
    val shirtNumber: Int? = null,
    /** Enum name or null; legacy free text is treated as "no position" (APP-RN-13). */
    val position: String? = null,
    val role: String = "PLAYER",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/** `ClubBanDto`, visible to managers. */
@Serializable
data class ClubBanDTO(
    val clubMemberId: String,
    val userId: String,
    val username: String,
    val bannedAt: String
)
