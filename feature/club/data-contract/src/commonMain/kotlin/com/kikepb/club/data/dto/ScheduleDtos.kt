package com.kikepb.club.data.dto

import kotlinx.serialization.Serializable

/** `ClubMatchScheduleDto` (BACKEND.md §9, BE-002 + BE-008). Times are `HH:mm:ss` in the club time zone. */
@Serializable
data class ClubMatchScheduleDTO(
    val id: String,
    val clubId: String,
    val matchDayOfWeek: String,
    val matchTime: String,
    val timeZone: String = "Europe/Madrid",
    val format: String = "ELEVEN_A_SIDE",
    val maxPlayers: Int = 22,
    val matchDurationMinutes: Int = 60,
    val closeDaysBefore: Int = 1,
    val closeTime: String = "22:00:00",
    val drawDaysBefore: Int = 1,
    val drawTime: String = "22:00:00",
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/** Body of `POST` and `PATCH /clubs/{id}/schedule`. Null fields are omitted (explicitNulls = false). */
@Serializable
data class ScheduleRequestDTO(
    val matchDayOfWeek: String? = null,
    val matchTime: String? = null,
    val timeZone: String? = null,
    val format: String? = null,
    val matchDurationMinutes: Int? = null,
    val isActive: Boolean? = null,
    val closeDaysBefore: Int? = null,
    val closeTime: String? = null,
    val drawDaysBefore: Int? = null,
    val drawTime: String? = null
)

/** `ScheduleExceptionDto` (BE-008 RN-B). */
@Serializable
data class ScheduleExceptionDTO(
    val id: String,
    val clubId: String,
    val date: String,
    val type: String,
    val newScheduledAt: String? = null,
    val reason: String? = null,
    val createdAt: String? = null
)

@Serializable
data class AddScheduleExceptionRequestDTO(
    val date: String,
    val type: String,
    val newScheduledAt: String? = null,
    val reason: String? = null
)
