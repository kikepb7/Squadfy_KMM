package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.repository.ChatRepository

class GetActiveParticipantsByChatIdUseCase(private val chatRepository: ChatRepository) {
    fun getActiveParticipantsByChatId(chatId: String) = chatRepository.getActiveParticipantsByChatId(chatId = chatId)
}