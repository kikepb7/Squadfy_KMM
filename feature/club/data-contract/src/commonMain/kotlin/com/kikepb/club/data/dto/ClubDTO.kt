package com.kikepb.club.data.dto

import kotlinx.serialization.Serializable

/** `ClubDto` of the backend API v1 (`BACKEND.md` §9). */
@Serializable
data class ClubDTO(
    val id: String,
    val name: String,
    val description: String? = null,
    val clubLogoUrl: String? = null,
    val ownerId: String,
    val invitationCode: String,
    val maxMembers: Int? = null,
    val membersCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/** `InvitationCodeDto` returned when a manager regenerates the code. */
@Serializable
data class InvitationCodeDTO(val invitationCode: String)
