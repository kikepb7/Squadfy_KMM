package com.kikepb.chat.domain.usecases.profile

import com.kikepb.chat.domain.repository.participant.ChatParticipantRepository

class UploadProfilePictureUseCase(
    private val chatParticipantRepository: ChatParticipantRepository
) {
    suspend fun uploadProfilePicture(imageBytes: ByteArray, mimeType: String) =
        chatParticipantRepository.uploadProfilePicture(imageBytes = imageBytes, mimeType = mimeType)
}