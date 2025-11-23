package com.kikepb.chat.domain.repository

import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result

interface ChatRepository {
    suspend fun createChat(otherUserIds: List<String>): Result<ChatModel, DataError.Remote>
}