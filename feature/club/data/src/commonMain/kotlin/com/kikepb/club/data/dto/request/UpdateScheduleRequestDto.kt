package com.kikepb.club.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateScheduleRequestDto(
    @SerialName("matchDayOfWeek") val matchDayOfWeek: String? = null,
    @SerialName("matchStartTime") val matchStartTime: String? = null,
    @SerialName("matchEndTime") val matchEndTime: String? = null,
    @SerialName("seasonStartMonth") val seasonStartMonth: Int? = null,
    @SerialName("seasonStartDay") val seasonStartDay: Int? = null,
    @SerialName("drawTime") val drawTime: String? = null
)
