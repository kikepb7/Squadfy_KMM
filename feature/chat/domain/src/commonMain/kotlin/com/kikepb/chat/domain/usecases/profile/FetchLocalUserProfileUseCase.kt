package com.kikepb.chat.domain.usecases.profile

import com.kikepb.chat.domain.repository.participant.ChatParticipantRepository

class FetchLocalUserProfileUseCase(private val chatParticipantRepository: ChatParticipantRepository) {
    suspend fun fetchLocalUserProfile() = chatParticipantRepository.fetchLocalParticipant()
}