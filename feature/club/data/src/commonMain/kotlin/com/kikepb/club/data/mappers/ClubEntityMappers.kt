package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.ClubDTO
import com.kikepb.club.data.dto.ClubMemberDTO
import com.kikepb.club.database.entity.ClubEntity
import com.kikepb.club.database.entity.ClubMemberEntity
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubModel

fun ClubDTO.toEntity(): ClubEntity = ClubEntity(
    clubId = id,
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

fun ClubMemberDTO.toEntity(): ClubMemberEntity = ClubMemberEntity(
    memberId = id,
    clubId = clubId,
    userId = userId,
    username = username,
    email = email,
    shirtNumber = shirtNumber,
    profilePictureUrl = profilePictureUrl,
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

fun ClubEntity.toDomain(): ClubModel = ClubModel(
    id = clubId,
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

fun ClubMemberEntity.toDomain(): ClubMemberModel = ClubMemberModel(
    id = memberId,
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

fun ClubModel.toEntity(): ClubEntity = ClubEntity(
    clubId = id,
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
