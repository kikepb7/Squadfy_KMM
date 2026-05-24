package com.kikepb.globalPosition.data.dto

import kotlinx.serialization.Serializable

@Serializable
enum class MatchStatusDto {
    SCHEDULED,
    IN_PROGRESS,
    FINISHED
}
