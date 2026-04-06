package com.kikepb.globalPosition.data.mapper

import com.kikepb.club.database.entity.ClubEntity
import com.kikepb.globalPosition.data.dto.ClubDto
import com.kikepb.globalPosition.domain.model.ClubModel

fun ClubEntity.toGlobalPositionDomain() = ClubModel(
    id = clubId,
    name = name,
    description = description,
    logoUrl = clubLogoUrl,
    invitationCode = invitationCode,
    membersCount = membersCount,
    maxMembers = maxMembers
)

fun ClubDto.toEntity() = ClubEntity(
    clubId = id,
    name = name,
    description = description,
    clubLogoUrl = clubLogoUrl,
    ownerId = ownerId,
    invitationCode = invitationCode,
    maxMembers = maxMembers,
    membersCount = membersCount
)
