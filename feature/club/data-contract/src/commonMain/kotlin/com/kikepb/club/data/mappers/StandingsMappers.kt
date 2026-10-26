package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.PlayerRatingDTO
import com.kikepb.club.data.dto.PlayerStatsDTO
import com.kikepb.club.data.dto.RatingLeaderboardEntryDTO
import com.kikepb.club.domain.model.MyRating
import com.kikepb.club.domain.model.PlayerStats
import com.kikepb.club.domain.model.RatingEntry
import com.kikepb.club.domain.model.StatsEntry

fun RatingLeaderboardEntryDTO.toDomain() = RatingEntry(
    rank = rank,
    clubMemberId = clubMemberId,
    rating = rating,
    matchesRated = matchesRated,
    isProvisional = isProvisional
)

fun PlayerRatingDTO.toDomain() = MyRating(
    clubMemberId = clubMemberId,
    rating = rating,
    matchesRated = matchesRated,
    isProvisional = isProvisional,
    rank = rank,
    totalPlayers = totalPlayers
)

fun PlayerStatsDTO.toDomain() = PlayerStats(
    clubMemberId = clubMemberId,
    matchesPlayed = matchesPlayed,
    wins = wins,
    draws = draws,
    losses = losses,
    goals = goals,
    assists = assists,
    yellowCards = yellowCards,
    redCards = redCards,
    minutesPlayed = minutesPlayed
)

/** Rows of `GET /stats` keep the backend order; a missing rank falls back to the row position. */
fun List<PlayerStatsDTO>.toStatsEntries(): List<StatsEntry> =
    mapIndexed { index, row -> StatsEntry(rank = row.rank ?: (index + 1), stats = row.toDomain()) }
