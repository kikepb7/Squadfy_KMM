package com.kikepb.chat.domain.usecases.profile

import com.kikepb.chat.domain.repository.participant.ChatParticipantRepository

class DeleteProfilePictureUseCase(
    private val chatParticipantRepository: ChatParticipantRepository
) {
    suspend fun deleteProfilePicture() = chatParticipantRepository.deleteProfilePicture()
}