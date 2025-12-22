package com.kikepb.chat.domain.usecases.message

import com.kikepb.chat.domain.repository.message.MessageRepository

class GetMessagesForChatUseCase(private val messageRepository: MessageRepository) {
    fun getMessagesForChat(chatId: String) = messageRepository.getMessagesForChat(chatId = chatId)
}