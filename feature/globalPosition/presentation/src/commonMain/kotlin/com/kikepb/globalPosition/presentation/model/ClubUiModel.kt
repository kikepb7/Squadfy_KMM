package com.kikepb.globalPosition.presentation.model

data class ClubUiModel(
    val id: String,
    val name: String,
    val description: String?,
    val logoUrl: String?,
    val invitationCode: String,
    val membersCount: Int,
    val maxMembers: Int?,
    val initials: String
)
