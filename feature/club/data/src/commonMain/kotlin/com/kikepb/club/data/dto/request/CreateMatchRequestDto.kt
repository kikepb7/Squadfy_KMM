package com.kikepb.club.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateMatchRequestDto(
    @SerialName("scheduledAt") val scheduledAt: String? = null,
    @SerialName("signupOpensAt") val signupOpensAt: String? = null,
    @SerialName("signupClosesAt") val signupClosesAt: String? = null
)
