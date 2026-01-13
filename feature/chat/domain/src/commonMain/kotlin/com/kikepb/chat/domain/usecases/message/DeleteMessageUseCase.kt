package com.kikepb.chat.domain.usecases.message

import com.kikepb.chat.domain.repository.message.MessageRepository

class DeleteMessageUseCase(private val messageRepository: MessageRepository) {
    suspend fun deleteMessage(messageId: String) = messageRepository.deleteMessage(messageId = messageId)
}