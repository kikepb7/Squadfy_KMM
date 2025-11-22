package com.kikepb.chat.domain.repository

import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

interface ChatParticipantRepository {
    suspend fun searchParticipant(query: String): Result<ChatParticipantModel, DataError.Remote>
}