package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.ClubDTO
import com.kikepb.club.data.dto.ClubMemberDTO
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel

fun ClubDTO.toDomain(): ClubModel = ClubModel(
    id = id,
    name = name,
    description = description,
    clubLogoUrl = clubLogoUrl,
    ownerId = ownerId,
    invitationCode = invitationCode,
    maxMembers = maxMembers,
    membersCount = membersCount
)

fun ClubMemberDTO.toDomain(): ClubMemberModel = ClubMemberModel(
    id = id,
    clubId = clubId,
    userId = userId,
    username = username,
    email = email,
    shirtNumber = shirtNumber,
    position = position,
    goalsScored = goalsScored,
    assists = assists,
    yellowCards = yellowCards,
    redCards = redCards,
    minutesPlayed = minutesPlayed,
    matchesPlayed = matchesPlayed,
    role = role
)
