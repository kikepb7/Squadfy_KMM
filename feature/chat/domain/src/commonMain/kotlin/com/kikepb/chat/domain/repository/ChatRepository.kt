package com.kikepb.chat.domain.repository

import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(): Flow<List<ChatModel>>
    suspend fun fetchChats(): Result<List<ChatModel>, DataError.Remote>
}