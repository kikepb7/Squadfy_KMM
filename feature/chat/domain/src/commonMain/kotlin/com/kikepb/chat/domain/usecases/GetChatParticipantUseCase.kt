package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.repository.ChatParticipantRepository

class GetChatParticipantUseCase(
    private val chatParticipantRepository: ChatParticipantRepository
) {
    suspend operator fun invoke(query: String) =
        chatParticipantRepository.searchParticipant(query)
}