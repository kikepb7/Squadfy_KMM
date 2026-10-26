package com.kikepb.club.data.dto

import kotlinx.serialization.Serializable

/** `RatingLeaderboardEntryDto` (BACKEND.md §9). */
@Serializable
data class RatingLeaderboardEntryDTO(
    val rank: Int,
    val clubMemberId: String,
    val rating: Int,
    val matchesRated: Int = 0,
    val isProvisional: Boolean = false
)

/** `PlayerRatingDto`. */
@Serializable
data class PlayerRatingDTO(
    val clubId: String? = null,
    val clubMemberId: String,
    val rating: Int,
    val matchesRated: Int = 0,
    val isProvisional: Boolean = false,
    val rank: Int,
    val totalPlayers: Int
)

/** `PlayerStatsDto`, and `ClubStatsEntryDto` when [rank] is present. */
@Serializable
data class PlayerStatsDTO(
    val rank: Int? = null,
    val clubMemberId: String,
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0,
    val goals: Int = 0,
    val assists: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0,
    val minutesPlayed: Int = 0
)
