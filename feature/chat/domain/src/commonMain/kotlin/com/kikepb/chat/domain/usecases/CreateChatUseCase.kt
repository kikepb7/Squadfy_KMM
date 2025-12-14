package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.repository.ChatService

class CreateChatUseCase(
    private val chatService: ChatService
) {
    suspend fun createChat(otherUserIds: List<String>) =
        chatService.createChat(otherUserIds = otherUserIds)
}