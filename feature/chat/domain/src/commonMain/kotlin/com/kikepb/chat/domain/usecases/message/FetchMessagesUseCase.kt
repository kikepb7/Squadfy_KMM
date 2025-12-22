package com.kikepb.chat.domain.usecases.message

import com.kikepb.chat.domain.repository.message.MessageRepository

class FetchMessagesUseCase(private val messageRepository: MessageRepository) {
    suspend fun fetchMessages(chatId: String, before: String?) = messageRepository.fetchMessages(chatId = chatId, before = before)
}