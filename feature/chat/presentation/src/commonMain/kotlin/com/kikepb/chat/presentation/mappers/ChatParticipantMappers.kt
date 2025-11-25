package com.kikepb.chat.presentation.mappers

import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi

fun ChatParticipantModel.toUi(): ChatParticipantModelUi =
    ChatParticipantModelUi(
        id = userId,
        username = username,
        initials = initials,
        imageUrl = profilePictureUrl
    )