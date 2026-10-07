package com.kikepb.club.domain.model

/** Row of the rating classification (`RatingLeaderboardEntryDto`): ties share the rank. */
data class RatingEntry(
    val rank: Int,
    val clubMemberId: String,
    val rating: Int,
    val matchesRated: Int,
    /** Few rated matches yet: the rating may still move a lot (APP-RN-09). */
    val isProvisional: Boolean
)

/** My rating and position (`PlayerRatingDto`). */
data class MyRating(
    val clubMemberId: String,
    val rating: Int,
    val matchesRated: Int,
    val isProvisional: Boolean,
    val rank: Int,
    val totalPlayers: Int
)

data class PlayerStats(
    val clubMemberId: String,
    val matchesPlayed: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goals: Int,
    val assists: Int,
    val yellowCards: Int,
    val redCards: Int,
    val minutesPlayed: Int
)

/** Row of the stats classification (`ClubStatsEntryDto`). */
data class StatsEntry(val rank: Int, val stats: PlayerStats)

/** `StatsSortBy` of the backend (default GOALS). */
enum class StatsSortBy { GOALS, ASSISTS, MATCHES, MINUTES, WINS }

/** Rating variation of a member in one completed match (BE-008 RN-F1). */
data class MatchRatingChange(val matchId: String, val scheduledAt: kotlin.time.Instant, val change: Int)
