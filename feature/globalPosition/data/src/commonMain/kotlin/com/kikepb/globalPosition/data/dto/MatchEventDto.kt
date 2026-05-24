package com.kikepb.globalPosition.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MatchEventDto(
    val playerId: String,
    val minute: Int? = null
)
