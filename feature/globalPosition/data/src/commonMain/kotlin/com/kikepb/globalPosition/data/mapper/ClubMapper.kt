package com.kikepb.globalPosition.data.mapper

import com.kikepb.globalPosition.data.dto.ClubDto
import com.kikepb.globalPosition.domain.model.ClubModel

fun ClubDto.toDomain() = ClubModel(
    id = id,
    name = name,
    description = description,
    logoUrl = clubLogoUrl,
    invitationCode = invitationCode,
    membersCount = membersCount,
    maxMembers = maxMembers
)
