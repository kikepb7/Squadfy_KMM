package com.kikepb.chat.domain.usecases.participant

import com.kikepb.chat.domain.repository.participant.ChatParticipantRepository

class FetchLocalParticipantUseCase(
    private val chatParticipantRepository: ChatParticipantRepository
) {
    suspend fun fetchLocalParticipant() = chatParticipantRepository.fetchLocalParticipant()
}