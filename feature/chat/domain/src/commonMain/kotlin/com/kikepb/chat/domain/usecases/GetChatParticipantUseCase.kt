package com.kikepb.chat.domain.usecases

import com.kikepb.chat.domain.repository.participant.ChatParticipantService

class GetChatParticipantUseCase(
    private val chatParticipantService: ChatParticipantService
) {
    suspend operator fun invoke(query: String) =
        chatParticipantService.searchParticipant(query)
}