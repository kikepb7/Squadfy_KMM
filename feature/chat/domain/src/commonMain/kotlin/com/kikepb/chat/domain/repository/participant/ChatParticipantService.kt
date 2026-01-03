package com.kikepb.chat.domain.repository.participant

import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

interface ChatParticipantService {
    suspend fun searchParticipant(query: String): Result<ChatParticipantModel, DataError.Remote>
    suspend fun getLocalParticipant(): Result<ChatParticipantModel, DataError.Remote>
}