package com.kikepb.chat.data.websocket.local

import com.kikepb.chat.data.datasource.remote.KtorChatMessageService
import com.kikepb.chat.data.dto.websocket.OutgoingWebSocketDTO
import com.kikepb.chat.data.dto.websocket.WebSocketMessageDTO
import com.kikepb.chat.data.mappers.toDomain
import com.kikepb.chat.data.mappers.toEntity
import com.kikepb.chat.data.mappers.toWebSocketDto
import com.kikepb.chat.data.network.KtorWebSocketConnector
import com.kikepb.chat.data.utils.PAGE_SIZE
import com.kikepb.chat.database.SquadfyChatDatabase
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus.FAILED
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.models.MessageWithSenderModel
import com.kikepb.chat.domain.models.OutgoingNewMessageModel
import com.kikepb.chat.domain.repository.message.MessageRepository
import com.kikepb.core.data.database.safeDatabaseUpdate
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.DataError.Local.NOT_FOUND
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.onFailure
import com.kikepb.core.domain.util.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class OfflineFirstMessageRepositoryImpl(
    private val database: SquadfyChatDatabase,
    private val chatMessageService: KtorChatMessageService,
    private val sessionStorage: SessionStorage,
    private val webSocketConnector: KtorWebSocketConnector,
    private val applicationScope: CoroutineScope,
    private val json: Json
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

    override suspend fun sendMessage(message: OutgoingNewMessageModel): EmptyResult<DataError> =
        safeDatabaseUpdate {
            val dto = message.toWebSocketDto()
            val localUser = sessionStorage.observeAuthInfo().first()?.user ?: return Result.Failure(error = NOT_FOUND)
            val messageEntity = dto.toEntity(
                senderId = localUser.id,
                deliveryStatus = ChatMessageDeliveryStatus.SENDING
            )

            database.chatMessageDao.upsertMessage(message = messageEntity)

            webSocketConnector.sendMessage(message = dto.toJsonPayload())
                .onFailure { error ->
                    applicationScope.launch {
                        database.chatMessageDao.upsertMessage(message = dto.toEntity(senderId = localUser.id, deliveryStatus = FAILED))
                    }.join()
                }
        }

    override fun getMessagesForChat(chatId: String): Flow<List<MessageWithSenderModel>> =
        database.chatMessageDao.getMessagesByChatId(chatId = chatId)
            .map { messages ->
                messages.map { it.toDomain() }
            }

    private fun OutgoingWebSocketDTO.NewMessage.toJsonPayload(): String {
        val webSocketMessage = WebSocketMessageDTO(
            type = type.name,
            payload = json.encodeToString(this)
        )
        return json.encodeToString(webSocketMessage)
    }
}