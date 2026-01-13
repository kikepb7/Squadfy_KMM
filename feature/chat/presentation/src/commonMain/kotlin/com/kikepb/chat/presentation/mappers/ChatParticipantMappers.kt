package com.kikepb.chat.presentation.mappers

import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi
import com.kikepb.core.domain.auth.model.UserModel

fun ChatParticipantModel.toUi(): ChatParticipantModelUi =
    ChatParticipantModelUi(
        id = userId,
        username = username,
        initials = initials,
        imageUrl = profilePictureUrl
    )

fun UserModel.toUi(): ChatParticipantModelUi =
    ChatParticipantModelUi(
        id = id,
        username = username,
        initials = username.take(n = 2).uppercase(),
        imageUrl = profilePictureUrl
    )