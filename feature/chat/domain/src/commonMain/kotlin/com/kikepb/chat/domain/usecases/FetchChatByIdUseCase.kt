package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.repository.ChatRepository

class FetchChatByIdUseCase(
    private val chatRepository: ChatRepository
) {
    suspend fun fetchChatById(chatId: String) = chatRepository.fetchChatById(chatId = chatId)
}