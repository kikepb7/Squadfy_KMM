package com.kikepb.club.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecordMatchResultRequestDto(
    @SerialName("teamAScore") val teamAScore: Int,
    @SerialName("teamBScore") val teamBScore: Int,
    @SerialName("playerStats") val playerStats: List<PlayerStatRequestDto> = emptyList()
)
