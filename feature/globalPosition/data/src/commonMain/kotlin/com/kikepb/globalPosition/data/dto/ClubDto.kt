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
    // Schedule fields are optional in the contract: defaults keep decoding working against
    // backends that do not send them yet (kotlinx.serialization treats params without defaults as required).
    val matchDayOfWeek: String? = null,
    val matchStartTime: String? = null,
    val matchEndTime: String? = null,
    val seasonStartMonth: Int = 9,
    val seasonStartDay: Int = 1,
    val drawTime: String = "18:00:00",
    val createdAt: String,
    val updatedAt: String
)
