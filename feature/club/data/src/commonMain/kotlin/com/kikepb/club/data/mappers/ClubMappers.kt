package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.ClubMatchDTO
import com.kikepb.club.data.dto.MatchParticipantDTO
import com.kikepb.club.data.dto.MatchSignupDTO
import com.kikepb.club.domain.model.ClubMatchModel
import com.kikepb.club.domain.model.MatchParticipantModel
import com.kikepb.club.domain.model.MatchSignupModel

// Legacy match flow mappers (pre-v1 routes), replaced in specs 005-007.

fun MatchParticipantDTO.toDomain(): MatchParticipantModel = MatchParticipantModel(
    signupId = signupId,
    clubMemberId = clubMemberId,
    displayName = displayName,
    position = position,
    rating = rating
)

fun ClubMatchDTO.toDomain(): ClubMatchModel = ClubMatchModel(
    id = id,
    clubId = clubId,
    scheduledAt = scheduledAt,
    signupOpensAt = signupOpensAt,
    signupClosesAt = signupClosesAt,
    status = status,
    teamAScore = teamAScore,
    teamBScore = teamBScore,
    teamA = teamA.map { it.toDomain() },
    teamB = teamB.map { it.toDomain() }
)

fun MatchSignupDTO.toDomain(): MatchSignupModel = MatchSignupModel(
    id = id,
    matchId = matchId,
    clubMemberId = clubMemberId,
    guestName = guestName,
    position = position,
    rating = rating,
    status = status,
    signedUpAt = signedUpAt
)
