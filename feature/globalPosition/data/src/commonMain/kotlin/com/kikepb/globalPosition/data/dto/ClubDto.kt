package com.kikepb.globalPosition.data.dto

import kotlinx.serialization.Serializable

typealias ClubId = String
typealias UserId = String

@Serializable
data class ClubDto(
    val id: ClubId,
    val name: String,
    val description: String?,
    val clubLogoUrl: String?,
    val ownerId: UserId,
    val invitationCode: String,
    val maxMembers: Int?,
    val membersCount: Int,
    val createdAt: String,
    val updatedAt: String
)
