package com.kikepb.club.domain.model

data class ClubModel(
    val id: String,
    val name: String,
    val description: String?,
    val clubLogoUrl: String?,
    val ownerId: String,
    val invitationCode: String,
    val maxMembers: Int?,
    val membersCount: Int
)
