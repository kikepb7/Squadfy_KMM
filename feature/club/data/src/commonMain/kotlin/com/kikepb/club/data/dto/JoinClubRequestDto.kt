package com.kikepb.club.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JoinClubRequestDto(
    @SerialName("invitationCode") val invitationCode: String,
    @SerialName("shirtNumber") val shirtNumber: Int? = null,
    @SerialName("position") val position: String? = null
)
