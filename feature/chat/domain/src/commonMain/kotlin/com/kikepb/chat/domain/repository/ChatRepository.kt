package com.kikepb.chat.domain.repository

import com.kikepb.chat.domain.models.ChatInfoModel
import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(): Flow<List<ChatModel>>
    fun getChatInfoById(chatId: String): Flow<ChatInfoModel>
    fun getActiveParticipantsByChatId(chatId: String): Flow<List<ChatParticipantModel>>
    suspend fun fetchChats(): Result<List<ChatModel>, DataError.Remote>
    suspend fun fetchChatById(chatId: String): EmptyResult<DataError.Remote>
    suspend fun createChat(otherUserIds: List<String>): Result<ChatModel, DataError.Remote>
    suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote>
    suspend fun addParticipantsToChat(chatId: String, userIds: List<String>): Result<ChatModel, DataError.Remote>
}