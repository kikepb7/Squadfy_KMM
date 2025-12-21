package com.kikepb.chat.data.websocket.remote

import com.kikepb.chat.data.dto.websocket.WebSocketMessageDTO
import com.kikepb.chat.data.mappers.toNewMessage
import com.kikepb.chat.data.network.KtorWebSocketConnector
import com.kikepb.chat.database.SquadfyChatDatabase
import com.kikepb.chat.domain.error.ConnectionErrorModel
import com.kikepb.chat.domain.models.ChatMessageDeliveryStatus.FAILED
import com.kikepb.chat.domain.models.ChatMessageModel
import com.kikepb.chat.domain.repository.ChatConnectionClient
import com.kikepb.chat.domain.repository.ChatRepository
import com.kikepb.chat.domain.repository.MessageRepository
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class WebSocketChatConnectionClient(
    private val webSocketConnector: KtorWebSocketConnector,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val database: SquadfyChatDatabase,
    private val sessionStorage: SessionStorage,
    private val json: Json
): ChatConnectionClient {

    override val chatMessages: Flow<ChatMessageModel>
        get() = TODO("Not yet implemented")

    override val connectionState = webSocketConnector.connectionState

    override suspend fun sendChatMessage(message: ChatMessageModel): EmptyResult<ConnectionErrorModel> {
        val outgoingDto = message.toNewMessage()
        val webSocketMessage = WebSocketMessageDTO(
            type = outgoingDto.type.name,
            payload = json.encodeToString(value = outgoingDto)
        )
        val rawJsonPayload = json.encodeToString(value = webSocketMessage)

        return webSocketConnector
            .sendMessage(message = rawJsonPayload)
            .onFailure { error ->
                messageRepository.updateMessageDeliveryStatus(messageId = message.id, status = FAILED)
            }
    }
}