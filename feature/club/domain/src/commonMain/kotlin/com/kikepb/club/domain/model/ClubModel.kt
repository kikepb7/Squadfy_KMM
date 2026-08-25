package com.kikepb.club.domain.model

data class ClubModel(
    val id: String,
    val name: String,
    val description: String?,
    val clubLogoUrl: String?,
    val ownerId: String,
    val invitationCode: String,
    val maxMembers: Int?,
    val membersCount: Int,
    val matchDayOfWeek: String?,
    val matchStartTime: String?,
    val matchEndTime: String?,
    val seasonStartMonth: Int,
    val seasonStartDay: Int,
    val drawTime: String
)
