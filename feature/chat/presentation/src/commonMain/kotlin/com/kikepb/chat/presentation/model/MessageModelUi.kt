package com.kikepb.chat.presentation.model

import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi
import com.kikepb.core.presentation.util.UiText

sealed interface MessageModelUi {
    data class LocalUserMessage(
        val id: String,
        val content: String,
        val deliveryStatus: ChatMessageDeliveryStatus,
        val isMenuOpen: Boolean,
        val formattedSentTime: UiText
    ): MessageModelUi

    data class OtherUserMessage(
        val id: String,
        val content: String,
        val formattedSentTime: UiText,
        val sender: ChatParticipantModelUi
    ): MessageModelUi

    data class DateSeparator(
        val id: String,
        val date: UiText,
    ): MessageModelUi
}