package com.kikepb.club.datafirebase.dto

import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.PlayerPosition

internal fun ClubDocDto.toDomain(id: String) = ClubModel(
    id = id,
    name = name,
    description = description,
    clubLogoUrl = logoUrl,
    ownerId = ownerId,
    invitationCode = invitationCode,
    maxMembers = maxMembers,
    membersCount = membersCount
)

internal fun ClubResponseDto.toDomain() = ClubModel(
    id = id,
    name = name,
    description = description,
    clubLogoUrl = clubLogoUrl,
    ownerId = ownerId,
    invitationCode = invitationCode,
    maxMembers = maxMembers,
    membersCount = membersCount
)

internal fun MemberDocDto.toDomain(documentId: String) = ClubMemberModel(
    id = id ?: documentId,
    clubId = clubId,
    userId = userId,
    username = username,
    profilePictureUrl = profilePictureUrl,
    shirtNumber = shirtNumber,
    position = PlayerPosition.fromRaw(position),
    role = ClubMemberRole.fromRaw(role),
    clubPictureUrl = clubPictureUrl
)

internal val MemberDocDto.isActive: Boolean get() = status == "ACTIVE"
