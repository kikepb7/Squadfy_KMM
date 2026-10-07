package com.kikepb.club.data.dto

import kotlinx.serialization.Serializable

/** `MatchEventDto` (BACKEND.md §9). */
@Serializable
data class MatchEventDTO(
    val id: String,
    val matchId: String? = null,
    val clubMemberId: String,
    val type: String,
    val minute: Int? = null,
    val createdAt: String
)

/** `MatchGuestDto` (BE-008). */
@Serializable
data class MatchGuestDTO(
    val guestId: String,
    val name: String,
    val position: String? = null,
    val invitedByMemberId: String? = null
)

/** `MatchDto` (BACKEND.md §9 + BE-008: guests, manual score, rating changes, schedule date). */
@Serializable
data class MatchDTO(
    val id: String,
    val clubId: String,
    val scheduledAt: String,
    val status: String,
    val enrolledPlayers: List<String> = emptyList(),
    val teamA: List<String> = emptyList(),
    val teamB: List<String> = emptyList(),
    val durationMinutes: Int = 60,
    val minutesPlayed: Map<String, Int> = emptyMap(),
    val enrolledGuests: List<MatchGuestDTO> = emptyList(),
    val teamAGuests: List<MatchGuestDTO> = emptyList(),
    val teamBGuests: List<MatchGuestDTO> = emptyList(),
    val isManualScore: Boolean = false,
    val ratingChanges: Map<String, Int> = emptyMap(),
    val scheduleDate: String? = null,
    val teamAScore: Int = 0,
    val teamBScore: Int = 0,
    val goals: List<MatchEventDTO> = emptyList(),
    val assists: List<MatchEventDTO> = emptyList(),
    val yellowCards: List<MatchEventDTO> = emptyList(),
    val redCards: List<MatchEventDTO> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class TeamPlayerRatingDTO(
    /** Member id, or the guestId when [isGuest]. */
    val clubMemberId: String,
    val rating: Int,
    val isGuest: Boolean = false
)

@Serializable
data class TeamStrengthDTO(
    val players: Int = 0,
    val averageRating: Int = 0,
    val totalRating: Int = 0,
    val playerRatings: List<TeamPlayerRatingDTO> = emptyList()
)

@Serializable
data class TeamBalanceDTO(
    val matchId: String? = null,
    val teamA: TeamStrengthDTO,
    val teamB: TeamStrengthDTO,
    val averageRatingDifference: Int = 0,
    val teamAExpectedScore: Double = 0.5
)

@Serializable
data class GenerateTeamsRequestDTO(
    val mode: String,
    val manualTeamA: List<String>? = null,
    val manualTeamB: List<String>? = null
)

@Serializable
data class CreateMatchRequestDTO(
    val scheduledAt: String,
    val format: String? = null,
    val durationMinutes: Int? = null
)

@Serializable
data class AddMatchEventRequestDTO(
    val clubMemberId: String,
    val type: String,
    val minute: Int? = null
)

@Serializable
data class SetPlayerMinutesRequestDTO(val minutes: Int)

@Serializable
data class MatchScoreRequestDTO(val teamAScore: Int, val teamBScore: Int)
