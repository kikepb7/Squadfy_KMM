package com.kikepb.globalPosition.data.mapper

import com.kikepb.globalPosition.data.dto.MatchDto
import com.kikepb.globalPosition.data.dto.MatchEventDto
import com.kikepb.globalPosition.data.dto.MatchStatusDto
import com.kikepb.globalPosition.data.dto.MatchStatusDto.FINISHED
import com.kikepb.globalPosition.data.dto.MatchStatusDto.IN_PROGRESS
import com.kikepb.globalPosition.data.dto.MatchStatusDto.SCHEDULED
import com.kikepb.globalPosition.domain.model.MatchEvent
import com.kikepb.globalPosition.domain.model.MatchModel
import com.kikepb.globalPosition.domain.model.MatchStatus

fun MatchDto.toDomain() = MatchModel(
    id = id,
    clubId = clubId,
    scheduledAt = scheduledAt,
    status = status.toDomain(),
    enrolledPlayers = enrolledPlayers,
    teamA = teamA,
    teamB = teamB,
    teamAScore = teamAScore,
    teamBScore = teamBScore,
    goals = goals.map { it.toDomain() },
    assists = assists.map { it.toDomain() },
    yellowCards = yellowCards.map { it.toDomain() },
    redCards = redCards.map { it.toDomain() }
)

fun MatchEventDto.toDomain() = MatchEvent(
    playerId = playerId,
    minute = minute
)

fun MatchStatusDto.toDomain() = when (this) {
    SCHEDULED -> MatchStatus.SCHEDULED
    IN_PROGRESS -> MatchStatus.IN_PROGRESS
    FINISHED -> MatchStatus.FINISHED
}
