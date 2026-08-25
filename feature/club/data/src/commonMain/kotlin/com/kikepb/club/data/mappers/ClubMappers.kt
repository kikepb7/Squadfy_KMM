package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.ClubDTO
import com.kikepb.club.data.dto.ClubMatchDTO
import com.kikepb.club.data.dto.ClubMemberDTO
import com.kikepb.club.data.dto.ClubScheduleExceptionDTO
import com.kikepb.club.data.dto.MatchParticipantDTO
import com.kikepb.club.data.dto.MatchSignupDTO
import com.kikepb.club.domain.model.ClubMatchModel
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.ClubScheduleExceptionModel
import com.kikepb.club.domain.model.MatchParticipantModel
import com.kikepb.club.domain.model.MatchSignupModel

fun ClubDTO.clubToDomain(): ClubModel = ClubModel(
    id = id,
    name = name,
    description = description,
    clubLogoUrl = clubLogoUrl,
    ownerId = ownerId,
    invitationCode = invitationCode,
    maxMembers = maxMembers,
    membersCount = membersCount,
    matchDayOfWeek = matchDayOfWeek,
    matchStartTime = matchStartTime,
    matchEndTime = matchEndTime,
    seasonStartMonth = seasonStartMonth,
    seasonStartDay = seasonStartDay,
    drawTime = drawTime
)

fun ClubMemberDTO.clubMemberToDomain(): ClubMemberModel = ClubMemberModel(
    id = id,
    clubId = clubId,
    userId = userId,
    username = username,
    email = email,
    profilePictureUrl = profilePictureUrl,
    shirtNumber = shirtNumber,
    position = position,
    rating = rating,
    goalsScored = goalsScored,
    assists = assists,
    yellowCards = yellowCards,
    redCards = redCards,
    minutesPlayed = minutesPlayed,
    matchesPlayed = matchesPlayed,
    role = role
)

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

fun ClubScheduleExceptionDTO.toDomain(): ClubScheduleExceptionModel = ClubScheduleExceptionModel(
    id = id,
    clubId = clubId,
    date = date,
    reason = reason
)
