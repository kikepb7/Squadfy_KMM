package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.repository.chat.ChatRepository

class DeleteAllChatsUseCase(private val chatRepository: ChatRepository) {
    suspend fun deleteAllChats() = chatRepository.deleteAllChats()
}