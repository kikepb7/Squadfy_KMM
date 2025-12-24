package com.kikepb.chat.domain.usecases.message

import com.kikepb.chat.domain.models.OutgoingNewMessageModel
import com.kikepb.chat.domain.repository.message.MessageRepository

class SendMessageUseCase(private val messageRepository: MessageRepository) {
    suspend fun sendMessage(message: OutgoingNewMessageModel) = messageRepository.sendMessage(message = message)
}