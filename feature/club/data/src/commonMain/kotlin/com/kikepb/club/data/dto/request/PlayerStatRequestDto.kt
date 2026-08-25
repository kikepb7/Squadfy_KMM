package com.kikepb.club.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerStatRequestDto(
    @SerialName("clubMemberId") val clubMemberId: String,
    @SerialName("goals") val goals: Int = 0,
    @SerialName("assists") val assists: Int = 0,
    @SerialName("yellowCards") val yellowCards: Int = 0,
    @SerialName("redCards") val redCards: Int = 0,
    @SerialName("minutesPlayed") val minutesPlayed: Int = 0
)
