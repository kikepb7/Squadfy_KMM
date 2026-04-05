package com.kikepb.club.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClubDTO(
    val id: String,
    val name: String,
    val description: String? = null,
    val clubLogoUrl: String? = null,
    val ownerId: String,
    val invitationCode: String,
    val maxMembers: Int? = null,
    val membersCount: Int
)