package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.repository.chat.ChatRepository

class GetChatInfoByIdUseCase(
    private val chatRepository: ChatRepository
) {
    fun getChatInfoById(chatId: String) = chatRepository.getChatInfoById(chatId = chatId)
}