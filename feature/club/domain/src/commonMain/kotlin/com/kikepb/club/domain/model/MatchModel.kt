package com.kikepb.club.domain.model

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** BE-004 RN-1: SCHEDULED → COMPLETED or CANCELLED (`IN_PROGRESS` exists but is not used). */
enum class MatchStatus { SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, UNKNOWN }

enum class MatchEventType { GOAL, ASSIST, YELLOW_CARD, RED_CARD }

enum class Team { A, B }

data class MatchEventModel(
    val id: String,
    val clubMemberId: String,
    val type: MatchEventType,
    val minute: Int?,
    val createdAt: Instant
)

/** A guest in a match (`MatchGuestDto`, BE-008 RN-A6): plays the draw with rating 1000, no stats. */
data class MatchGuestModel(
    val guestId: String,
    val name: String,
    val position: PlayerPosition?,
    val invitedByMemberId: String?
)

/** A match (`MatchDto`). Teams hold `clubMemberId`s plus guests (BE-008). */
data class MatchModel(
    val id: String,
    val clubId: String,
    val scheduledAt: Instant,
    val status: MatchStatus,
    val enrolledPlayers: List<String>,
    val enrolledGuests: List<MatchGuestModel>,
    val teamA: List<String>,
    val teamB: List<String>,
    val teamAGuests: List<MatchGuestModel>,
    val teamBGuests: List<MatchGuestModel>,
    val durationMinutes: Int,
    /** Effective minutes per member of the teams; absent = the full duration (BE-004 RN-6). */
    val minutesPlayed: Map<String, Int>,
    /** Official score: the manual one when [isManualScore], otherwise the goal events (BE-008 RN-E2). */
    val teamAScore: Int,
    val teamBScore: Int,
    val isManualScore: Boolean,
    val events: List<MatchEventModel>,
    /** Rating delta per member in a completed match: the "match rating" (BE-008 RN-F1). */
    val ratingChanges: Map<String, Int>,
    /** Week of the schedule this match belongs to; null for extra matches. */
    val scheduleDate: LocalDate?
) {
    val hasTeams: Boolean get() = teamA.isNotEmpty() || teamB.isNotEmpty() || teamAGuests.isNotEmpty() || teamBGuests.isNotEmpty()

    fun teamOf(clubMemberId: String): Team? = when (clubMemberId) {
        in teamA -> Team.A
        in teamB -> Team.B
        else -> null
    }

    fun minutesOf(clubMemberId: String): Int = minutesPlayed[clubMemberId] ?: durationMinutes
}

data class PlayerRatingInTeam(val id: String, val rating: Int, val isGuest: Boolean)

data class TeamStrengthModel(
    val players: Int,
    val averageRating: Int,
    val totalRating: Int,
    /** Highest rating first; guests appear with their `guestId` and 1000. */
    val playerRatings: List<PlayerRatingInTeam>
)

/** Team balance for managers (`TeamBalanceDto`, BE-003 RN-10). */
data class TeamBalanceModel(
    val teamA: TeamStrengthModel,
    val teamB: TeamStrengthModel,
    val averageRatingDifference: Int,
    /** Expected score of team A between 0 and 1 (0.5 = even). */
    val teamAExpectedScore: Double
)
