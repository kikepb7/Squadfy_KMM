package com.kikepb.chat.presentation.provider

import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.presentation.model.MessageModelUi
import com.kikepb.core.designsystem.components.avatar.ChatParticipantModelUi
import com.kikepb.core.presentation.util.UiText
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

class MessageListItemProvider  : PreviewParameterProvider<MessageModelUi> {
    override val values: Sequence<MessageModelUi> = sequenceOf(
        MessageModelUi.LocalUserMessage(
            id = "1",
            content = "Hello world, this is a preview message that spans multiple lines",
            deliveryStatus = ChatMessageDeliveryStatus.SENT,
            formattedSentTime = UiText.DynamicString("Friday 2:20pm")
        ),
        MessageModelUi.LocalUserMessage(
            id = "2",
            content = "Hello world, this is a preview message that spans multiple lines",
            deliveryStatus = ChatMessageDeliveryStatus.FAILED,
            formattedSentTime = UiText.DynamicString("Friday 2:20pm")
        ),
        MessageModelUi.OtherUserMessage(
            id = "3",
            content = "Hello world, this is a preview message that spans multiple lines",
            formattedSentTime = UiText.DynamicString("Friday 2:20pm"),
            sender = ChatParticipantModelUi(
                id = "1",
                username = "Enrique",
                initials = "EN"
            )
        )
    )
}