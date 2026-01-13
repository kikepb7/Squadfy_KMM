package com.kikepb.chat.presentation.model

import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi
import com.kikepb.core.presentation.util.UiText

sealed class MessageModelUi(open val id: String) {
    data class LocalUserMessage(
        override val id: String,
        val content: String,
        val deliveryStatus: ChatMessageDeliveryStatus,
        val formattedSentTime: UiText
    ): MessageModelUi(id = id)

    data class OtherUserMessage(
        override val id: String,
        val content: String,
        val formattedSentTime: UiText,
        val sender: ChatParticipantModelUi
    ): MessageModelUi(id = id)

    data class DateSeparator(
        override val id: String,
        val date: UiText,
    ): MessageModelUi(id = id)
}