package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.repository.ChatRepository

class AddParticipantsToChatUseCase(private val chatRepository: ChatRepository) {
    suspend fun addParticipantsToChat(chatId: String, userIds: List<String>) =
        chatRepository.addParticipantsToChat(chatId = chatId, userIds = userIds)
}