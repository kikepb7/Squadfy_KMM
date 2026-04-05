package com.kikepb.globalPosition.presentation.mapper

import com.kikepb.globalPosition.domain.model.ClubModel
import com.kikepb.globalPosition.presentation.model.ClubUiModel

fun ClubModel.toUiModel() = ClubUiModel(
    id = id,
    name = name,
    description = description,
    logoUrl = logoUrl,
    invitationCode = invitationCode,
    membersCount = membersCount,
    maxMembers = maxMembers,
    initials = name
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { name.take(2).uppercase() }
)
