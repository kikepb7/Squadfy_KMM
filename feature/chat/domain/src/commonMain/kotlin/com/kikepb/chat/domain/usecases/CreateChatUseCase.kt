package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.repository.ChatRepository

class CreateChatUseCase(
    private val chatRepository: ChatRepository
) {
    suspend fun createChat(otherUserIds: List<String>) =
        chatRepository.createChat(otherUserIds = otherUserIds)
}