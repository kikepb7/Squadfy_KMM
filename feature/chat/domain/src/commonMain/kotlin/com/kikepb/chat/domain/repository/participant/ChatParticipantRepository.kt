package com.kikepb.chat.domain.repository.participant

import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result

interface ChatParticipantRepository {
    suspend fun fetchLocalParticipant(): Result<ChatParticipantModel, DataError>
    suspend fun uploadProfilePicture(imageBytes: ByteArray, mimeType: String): EmptyResult<DataError.Remote>
}