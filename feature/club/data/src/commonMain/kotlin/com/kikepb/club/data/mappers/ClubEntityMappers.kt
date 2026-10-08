package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.ClubDTO
import com.kikepb.club.data.dto.ClubMemberDTO
import com.kikepb.club.database.entity.ClubEntity
import com.kikepb.club.database.entity.ClubMemberEntity
import com.kikepb.club.domain.model.ClubMemberModel
import com.kikepb.club.domain.model.ClubMemberRole
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.domain.model.PlayerPosition

fun ClubDTO.toEntity(): ClubEntity = ClubEntity(
    clubId = id,
    name = name,
    description = description,
    clubLogoUrl = clubLogoUrl,
    ownerId = ownerId,
    invitationCode = invitationCode,
    maxMembers = maxMembers,
    membersCount = membersCount
)

/** Unknown roles and legacy free-text positions are normalized before being cached (APP-RN-13). */
fun ClubMemberDTO.toEntity(): ClubMemberEntity = ClubMemberEntity(
    memberId = id,
    clubId = clubId,
    userId = userId,
    username = username,
    shirtNumber = shirtNumber,
    profilePictureUrl = profilePictureUrl,
    position = PlayerPosition.fromRaw(position)?.name,
    role = ClubMemberRole.fromRaw(role).name,
    clubPictureUrl = clubPictureUrl
)

fun ClubEntity.toDomain(): ClubModel = ClubModel(
    id = clubId,
    name = name,
    description = description,
    clubLogoUrl = clubLogoUrl,
    ownerId = ownerId,
    invitationCode = invitationCode,
    maxMembers = maxMembers,
    membersCount = membersCount
)

fun ClubMemberEntity.toDomain(): ClubMemberModel = ClubMemberModel(
    id = memberId,
    clubId = clubId,
    userId = userId,
    username = username,
    profilePictureUrl = profilePictureUrl,
    shirtNumber = shirtNumber,
    position = PlayerPosition.fromRaw(position),
    role = ClubMemberRole.fromRaw(role),
    clubPictureUrl = clubPictureUrl
)

fun ClubDTO.toDomain(): ClubModel = toEntity().toDomain()

fun ClubMemberDTO.toDomain(): ClubMemberModel = toEntity().toDomain()
