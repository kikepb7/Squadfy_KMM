package com.kikepb.chat.data.websocket.remote

import com.kikepb.chat.data.dto.websocket.IncomingWebSocketDTO
import com.kikepb.chat.data.dto.websocket.IncomingWebSocketDTO.ChatParticipantChangedDTO
import com.kikepb.chat.data.dto.websocket.IncomingWebSocketDTO.MessageDeletedDTO
import com.kikepb.chat.data.dto.websocket.IncomingWebSocketDTO.NewMessageDTO
import com.kikepb.chat.data.dto.websocket.IncomingWebSocketDTO.ProfilePictureUpdatedDTO
import com.kikepb.chat.data.dto.websocket.IncomingWebSocketType.CHAT_PARTICIPANTS_CHANGED
import com.kikepb.chat.data.dto.websocket.IncomingWebSocketType.MESSAGE_DELETED
import com.kikepb.chat.data.dto.websocket.IncomingWebSocketType.NEW_MESSAGE
import com.kikepb.chat.data.dto.websocket.IncomingWebSocketType.PROFILE_PICTURE_UPDATED
import com.kikepb.chat.data.dto.websocket.WebSocketMessageDTO
import com.kikepb.chat.data.mappers.toDomain
import com.kikepb.chat.data.mappers.toEntity
import com.kikepb.chat.data.network.KtorWebSocketConnector
import com.kikepb.chat.data.utils.STOP_TIMEOUT_MILLIS
import com.kikepb.chat.database.SquadfyChatDatabase
import com.kikepb.chat.domain.repository.chat.ChatConnectionClient
import com.kikepb.chat.domain.repository.chat.ChatRepository
import com.kikepb.core.domain.auth.repository.SessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json

class WebSocketChatConnectionClient(
    private val webSocketConnector: KtorWebSocketConnector,
    private val chatRepository: ChatRepository,
    private val database: SquadfyChatDatabase,
    private val sessionStorage: SessionStorage,
    private val json: Json,
    private val applicationScope: CoroutineScope
): ChatConnectionClient {

    override val chatMessages = webSocketConnector
        .messages
        .mapNotNull { parseIncomingMessage(message = it) }
        .onEach { handleIncomingMessage(message = it) }
        .filterIsInstance<NewMessageDTO>()
        .mapNotNull { message ->
            database.chatMessageDao.getMessageById(messageId = message.id)?.toDomain()
        }
        .shareIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = STOP_TIMEOUT_MILLIS)
        )

    override val connectionState = webSocketConnector.connectionState

    private fun parseIncomingMessage(message: WebSocketMessageDTO): IncomingWebSocketDTO? =
        when (message.type) {
            NEW_MESSAGE.name -> {
                json.decodeFromString<NewMessageDTO>(message.payload)
            }
            MESSAGE_DELETED.name -> {
                json.decodeFromString<MessageDeletedDTO>(message.payload)
            }
            PROFILE_PICTURE_UPDATED.name -> {
                json.decodeFromString<ProfilePictureUpdatedDTO>(message.payload)
            }
            CHAT_PARTICIPANTS_CHANGED.name -> {
                json.decodeFromString<ChatParticipantChangedDTO>(message.payload)
            }
            else -> null
        }

    private suspend fun handleIncomingMessage(message: IncomingWebSocketDTO) {
        when (message) {
            is ChatParticipantChangedDTO -> refreshChat(message = message)
            is MessageDeletedDTO -> deleteMessage(message = message)
            is NewMessageDTO -> handleNewMessage(message = message)
            is ProfilePictureUpdatedDTO -> updateProfilePicture(message = message)
        }
    }

    private suspend fun refreshChat(message: ChatParticipantChangedDTO) =
        chatRepository.fetchChatById(chatId = message.chatId)

    private suspend fun deleteMessage(message: MessageDeletedDTO) =
        database.chatMessageDao.deleteMessageById(messageId = message.messageId)

    private suspend fun handleNewMessage(message: NewMessageDTO) {
        val chatExists = database.chatDao.getChatById(id = message.chatId) != null
        if (chatExists) chatRepository.fetchChatById(chatId = message.chatId)

        val messageEntity = message.toEntity()
        database.chatDao.updateLastActivity(messageEntity.chatId, messageEntity.timestamp)
        database.chatMessageDao.upsertMessage(message = messageEntity)
    }

    private suspend fun updateProfilePicture(message: ProfilePictureUpdatedDTO) {
        database.chatParticipantDao.updateProfilePictureUrl(userId = message.userId, newUrl = message.newUrl)

        val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
        if (authInfo != null && authInfo.user.id == message.userId) sessionStorage.set(
            info = authInfo.copy(
                user = authInfo.user.copy(profilePictureUrl = message.newUrl)
            )
        )
    }
}