package com.kikepb.chat.domain.models

data class MessageWithSenderModel(
    val message: ChatMessageModel,
    val sender: ChatParticipantModel,
    val deliveryStatus: ChatMessageDeliveryStatus?
)
