package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.repository.chat.ChatRepository

class GetChatsUseCase(
    private val chatRepository: ChatRepository
) {
    fun getChats() = chatRepository.getChats()
}