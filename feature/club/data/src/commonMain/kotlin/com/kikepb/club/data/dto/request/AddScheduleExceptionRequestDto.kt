package com.kikepb.club.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddScheduleExceptionRequestDto(
    @SerialName("date") val date: String,
    @SerialName("reason") val reason: String? = null
)
