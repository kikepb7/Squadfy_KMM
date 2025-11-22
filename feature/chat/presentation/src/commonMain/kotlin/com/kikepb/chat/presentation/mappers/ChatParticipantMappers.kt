package com.kikepb.chat.presentation.mappers

import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.presentation.create_chat.model.ChatParticipantUiModel

fun ChatParticipantModel.toUi(): ChatParticipantUiModel =
    ChatParticipantUiModel(
        id = userId,
        username = username,
        initials = initials,
        imageUrl = profilePictureUrl
    )