package com.kikepb.club.data.dto.request

import kotlinx.serialization.Serializable

// Requests of the club administration endpoints (BACKEND.md §8.2). Null fields are omitted (explicitNulls = false).

@Serializable
data class EditClubRequestDto(
    val name: String? = null,
    val description: String? = null,
    val maxMembers: Int? = null
)

@Serializable
data class UpdateMyMembershipRequestDto(
    val shirtNumber: Int? = null,
    val position: String? = null
)

@Serializable
data class ChangeRoleRequestDto(val role: String)

@Serializable
data class TransferOwnershipRequestDto(val memberId: String)
