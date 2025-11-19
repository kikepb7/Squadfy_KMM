package com.kikepb.chat.domain.models

data class ChatInfoModel(
    val chat: ChatModel,
    val messages: List<MessageWithSenderModel>
)
