package com.kikepb.chat.data.websocket.local

import com.kikepb.chat.data.datasource.remote.KtorChatMessageService
import com.kikepb.chat.data.mappers.toDomain
import com.kikepb.chat.data.mappers.toEntity
import com.kikepb.chat.data.utils.PAGE_SIZE
import com.kikepb.chat.database.SquadfyChatDatabase
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.models.MessageWithSenderModel
import com.kikepb.chat.domain.repository.message.MessageRepository
import com.kikepb.core.data.database.safeDatabaseUpdate
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class OfflineFirstMessageRepositoryImpl(
    private val database: SquadfyChatDatabase,
    private val chatMessageService: KtorChatMessageService
): MessageRepository {

    override suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local> =
        safeDatabaseUpdate {
            database.chatMessageDao.updateDeliveryStatus(
                messageId = messageId,
                status = status.name,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        }

    override suspend fun fetchMessages(
        chatId: String,
        before: String?
    ): Result<List<ChatMessageModel>, DataError> =
        chatMessageService.fetchMessages(chatId = chatId, before = before)
            .onSuccess { messages ->
                return safeDatabaseUpdate {
                    database.chatMessageDao.upsertMessagesAndSyncIfNecessary(
                        chatId = chatId,
                        serverMessages = messages.map { it.toEntity() },
                        pageSize = PAGE_SIZE,
                        shouldSync = before == null // Only sync for most recent page
                    )
                    messages
                }
            }

    override fun getMessagesForChat(chatId: String): Flow<List<MessageWithSenderModel>> =
        database.chatMessageDao.getMessagesByChatId(chatId = chatId)
            .map { messages ->
                messages.map { it.toDomain() }
            }
}