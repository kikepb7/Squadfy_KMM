package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.repository.ChatRepository

class LeaveChatUseCase(private val chatRepository: ChatRepository) {
    suspend fun leaveChat(chatId: String) = chatRepository.leaveChat(chatId = chatId)
}