package com.kikepb.club.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateClubMemberRequestDto(
    @SerialName("shirtNumber") val shirtNumber: Int? = null,
    @SerialName("position") val position: String? = null,
    @SerialName("rating") val rating: Int? = null
)
