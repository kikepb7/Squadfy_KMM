package com.kikepb.club.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddGuestRequestDto(
    @SerialName("guestName") val guestName: String,
    @SerialName("position") val position: String? = null,
    @SerialName("rating") val rating: Int? = null
)
