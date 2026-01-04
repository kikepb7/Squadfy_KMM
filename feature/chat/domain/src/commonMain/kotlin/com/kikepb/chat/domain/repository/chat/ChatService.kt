package com.kikepb.chat.domain.repository.chat

import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result

interface ChatService {
    suspend fun createChat(otherUserIds: List<String>): Result<ChatModel, DataError.Remote>
    suspend fun getChats(): Result<List<ChatModel>, DataError.Remote>
    suspend fun getChatById(chatId: String): Result<ChatModel, DataError.Remote>
    suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote>
    suspend fun addParticipantsToChat(chatId: String, userIds: List<String>): Result<ChatModel, DataError.Remote>
}