package com.kikepb.globalPosition.data.dto

import kotlinx.serialization.Serializable

typealias ClubId = String
typealias UserId = String

/** `ClubDto` of the backend API v1 (`GET /clubs`). The weekly schedule is a separate resource (spec 004). */
@Serializable
data class ClubDto(
    val id: ClubId,
    val name: String,
    val description: String? = null,
    val clubLogoUrl: String? = null,
    val ownerId: UserId,
    val invitationCode: String,
    val maxMembers: Int? = null,
    val membersCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
