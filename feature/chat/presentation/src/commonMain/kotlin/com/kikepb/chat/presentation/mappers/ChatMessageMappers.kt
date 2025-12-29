package com.kikepb.chat.presentation.mappers

import com.kikepb.chat.domain.models.MessageWithSenderModel
import com.kikepb.chat.presentation.model.MessageModelUi
import com.kikepb.chat.presentation.util.DateUtils

fun MessageWithSenderModel.toUi(localUserId: String, ): MessageModelUi {
    val isFromLocalUser = this.sender.userId == localUserId

    return if (isFromLocalUser) {
        MessageModelUi.LocalUserMessage(
            id = message.id,
            content = message.content,
            deliveryStatus =  message.deliveryStatus,
            formattedSentTime = DateUtils.formatMessageTime(instant = message.createdAt)
        )
    } else {
        MessageModelUi.OtherUserMessage(
            id = message.id,
            content = message.content,
            formattedSentTime = DateUtils.formatMessageTime(instant = message.createdAt),
            sender = sender.toUi()
        )
    }
}