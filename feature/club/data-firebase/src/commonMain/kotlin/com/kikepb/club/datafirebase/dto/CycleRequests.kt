package com.kikepb.club.datafirebase.dto

import kotlinx.serialization.Serializable

// Inputs of the weekly cycle callables (`firebase/functions/src/match`): the backend request bodies plus the ids the
// REST routes carried in the path.

@Serializable internal data class MatchIdRequest(val matchId: String)
@Serializable internal data class AnnouncementIdRequest(val announcementId: String)
@Serializable internal data class AddGuestRequest(val announcementId: String, val name: String, val position: String?)
@Serializable internal data class RemoveGuestRequest(val announcementId: String, val guestId: String)

@Serializable
internal data class ScheduleRequest(
    val clubId: String,
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

@Serializable internal data class AddExceptionRequest(val clubId: String, val date: String, val type: String, val newScheduledAt: String?, val reason: String?)
@Serializable internal data class DeleteExceptionRequest(val clubId: String, val exceptionId: String)

@Serializable internal data class ClubMatchesRequest(val clubId: String, val status: String?)
@Serializable internal data class CreateExtraMatchRequest(val clubId: String, val scheduledAt: String, val format: String?, val durationMinutes: Int?)
@Serializable internal data class GenerateTeamsRequest(val matchId: String, val mode: String, val manualTeamA: List<String>?, val manualTeamB: List<String>?)
@Serializable internal data class AddEventRequest(val matchId: String, val clubMemberId: String, val type: String, val minute: Int?)
@Serializable internal data class DeleteEventRequest(val matchId: String, val eventId: String)
@Serializable internal data class SetMinutesRequest(val matchId: String, val clubMemberId: String, val minutes: Int)
@Serializable internal data class SetScoreRequest(val matchId: String, val teamAScore: Int, val teamBScore: Int)

@Serializable internal data class StatsRequest(val clubId: String, val sortBy: String, val from: String?, val to: String?)
@Serializable internal data class AbsencesRequest(val clubId: String, val from: String?, val to: String?)
@Serializable internal data class CreateAbsenceRequest(val clubId: String, val fromDate: String, val toDate: String, val reason: String?)
@Serializable internal data class DeleteAbsenceRequest(val clubId: String, val absenceId: String)

@Serializable internal data class NotificationSettingsDto(val clubId: String? = null, val muted: Boolean = false)
@Serializable internal data class SetMutedRequest(val clubId: String, val muted: Boolean)

/** `clubs/{clubId}/live/changes`, written by the Functions after each change (`src/live.ts`). */
@Serializable internal data class LiveChangeDto(val scope: String? = null, val matchId: String? = null)
