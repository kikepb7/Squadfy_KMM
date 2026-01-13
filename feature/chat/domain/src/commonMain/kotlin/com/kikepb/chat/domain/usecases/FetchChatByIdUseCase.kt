package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.repository.chat.ChatRepository

class FetchChatByIdUseCase(
    private val chatRepository: ChatRepository
) {
    suspend fun fetchChatById(chatId: String) = chatRepository.fetchChatById(chatId = chatId)
}