package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.repository.ChatRepository

class FetchChatsUseCase(
    private val chatRepository: ChatRepository
) {
    suspend fun fetchChats() = chatRepository.fetchChats()
}