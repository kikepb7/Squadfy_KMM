package com.kikepb.club.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class JoinClubRequestDTO(
    val invitationCode: String,
    val shirtNumber: Int? = null,
    val position: String? = null
)
