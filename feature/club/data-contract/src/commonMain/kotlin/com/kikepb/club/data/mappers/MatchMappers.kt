package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.MatchDTO
import com.kikepb.club.data.dto.MatchEventDTO
import com.kikepb.club.data.dto.MatchGuestDTO
import com.kikepb.club.data.dto.TeamBalanceDTO
import com.kikepb.club.data.dto.TeamStrengthDTO
import com.kikepb.club.domain.model.MatchEventModel
import com.kikepb.club.domain.model.MatchEventType
import com.kikepb.club.domain.model.MatchGuestModel
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.model.PlayerRatingInTeam
import com.kikepb.club.domain.model.TeamBalanceModel
import com.kikepb.club.domain.model.TeamStrengthModel
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

fun MatchEventDTO.toDomain(): MatchEventModel? {
    val eventType = MatchEventType.entries.firstOrNull { it.name == type } ?: return null
    return MatchEventModel(
        id = id,
        clubMemberId = clubMemberId,
        type = eventType,
        minute = minute,
        createdAt = Instant.parse(createdAt)
    )
}

fun MatchGuestDTO.toDomain(): MatchGuestModel = MatchGuestModel(
    guestId = guestId,
    name = name,
    position = PlayerPosition.fromRaw(position),
    invitedByMemberId = invitedByMemberId
)

fun MatchDTO.toDomain(): MatchModel = MatchModel(
    id = id,
    clubId = clubId,
    scheduledAt = Instant.parse(scheduledAt),
    status = MatchStatus.entries.firstOrNull { it.name == status } ?: MatchStatus.UNKNOWN,
    enrolledPlayers = enrolledPlayers,
    enrolledGuests = enrolledGuests.map { it.toDomain() },
    teamA = teamA,
    teamB = teamB,
    teamAGuests = teamAGuests.map { it.toDomain() },
    teamBGuests = teamBGuests.map { it.toDomain() },
    durationMinutes = durationMinutes,
    minutesPlayed = minutesPlayed,
    teamAScore = teamAScore,
    teamBScore = teamBScore,
    isManualScore = isManualScore,
    // Chronological: by minute (events without minute last), then by creation
    events = (goals + assists + yellowCards + redCards)
        .mapNotNull { it.toDomain() }
        .sortedWith(compareBy<MatchEventModel> { it.minute ?: Int.MAX_VALUE }.thenBy { it.createdAt }),
    ratingChanges = ratingChanges,
    scheduleDate = scheduleDate?.let(LocalDate::parse)
)

private fun TeamStrengthDTO.toDomain(): TeamStrengthModel = TeamStrengthModel(
    players = players,
    averageRating = averageRating,
    totalRating = totalRating,
    playerRatings = playerRatings
        .map { PlayerRatingInTeam(id = it.clubMemberId, rating = it.rating, isGuest = it.isGuest) }
        .sortedByDescending { it.rating }
)

fun TeamBalanceDTO.toDomain(): TeamBalanceModel = TeamBalanceModel(
    teamA = teamA.toDomain(),
    teamB = teamB.toDomain(),
    averageRatingDifference = averageRatingDifference,
    teamAExpectedScore = teamAExpectedScore
)
