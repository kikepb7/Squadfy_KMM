package com.kikepb.globalPosition.domain.model

data class ClubModel(
    val id: String,
    val name: String,
    val description: String?,
    val logoUrl: String?,
    val invitationCode: String,
    val membersCount: Int,
    val maxMembers: Int?
)
