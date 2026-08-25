package com.kikepb.club.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenerateTeamsRequestDto(
    @SerialName("mode") val mode: String,
    @SerialName("manualTeamA") val manualTeamA: List<String>? = null,
    @SerialName("manualTeamB") val manualTeamB: List<String>? = null
)
