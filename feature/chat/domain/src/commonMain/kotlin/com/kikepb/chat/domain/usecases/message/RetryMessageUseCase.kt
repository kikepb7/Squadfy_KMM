package com.kikepb.chat.domain.usecases.message

import com.kikepb.chat.domain.repository.message.MessageRepository

class RetryMessageUseCase(private val messageRepository: MessageRepository) {
    suspend fun retryMessage(messageId: String) = messageRepository.retryMessage(messageId = messageId)
}