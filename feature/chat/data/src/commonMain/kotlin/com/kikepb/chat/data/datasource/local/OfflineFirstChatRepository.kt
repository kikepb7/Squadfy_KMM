package com.kikepb.chat.data.datasource.local

import com.kikepb.chat.data.mappers.toDomain
import com.kikepb.chat.data.mappers.toEntity
import com.kikepb.chat.data.mappers.toLastMessageView
import com.kikepb.chat.database.SquadfyChatDatabase
import com.kikepb.chat.database.entities.ChatWithParticipants
import com.kikepb.chat.domain.models.ChatInfoModel
import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.chat.domain.repository.ChatRepository
import com.kikepb.chat.domain.repository.ChatService
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.asEmptyResult
import com.kikepb.core.domain.util.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class OfflineFirstChatRepository(
    private val chatService: ChatService,
    private val db: SquadfyChatDatabase
): ChatRepository {

    override fun getChats(): Flow<List<ChatModel>> =
        db.chatDao.getChatsWithActiveParticipants()
            .map { chatWithParticipantsList ->
                chatWithParticipantsList.map { it.toDomain() }
            }

    override fun getChatInfoById(chatId: String): Flow<ChatInfoModel> =
        db.chatDao.getChatInfoById(chatId = chatId)
            .filterNotNull()
            .map { it.toDomain() }

    override suspend fun fetchChats(): Result<List<ChatModel>, DataError.Remote> =
        chatService
            .getChats()
            .onSuccess { chats ->
                val chatsWithParticipants = chats.map { chat ->
                    ChatWithParticipants(
                        chat = chat.toEntity(),
                        participants = chat.participants.map { it.toEntity() },
                        lastMessage = chat.lastMessage?.toLastMessageView()
                    )
                }

                db.chatDao.upsertChatsWithParticipantsAndCrossRefs(
                    chats = chatsWithParticipants,
                    participantDao = db.chatParticipantDao,
                    crossRefDao = db.chatParticipantsCrossRefDao,
                    messageDao = db.chatMessageDao
                )
            }

    override suspend fun fetchChatById(chatId: String): EmptyResult<DataError.Remote> =
        chatService.getChatById(chatId = chatId)
            .onSuccess { chat ->
                db.chatDao.upsertChatWithParticipantsAndCrossRefs(
                    chat = chat.toEntity(),
                    participants = chat.participants.map { it.toEntity() },
                    participantDao = db.chatParticipantDao,
                    crossRefDao = db.chatParticipantsCrossRefDao
                )
            }
            .asEmptyResult()

    override suspend fun createChat(otherUserIds: List<String>): Result<ChatModel, DataError.Remote> =
        chatService.createChat(otherUserIds = otherUserIds)
            .onSuccess { chat ->
                db.chatDao.upsertChatWithParticipantsAndCrossRefs(
                    chat = chat.toEntity(),
                    participants = chat.participants.map { it.toEntity() },
                    participantDao = db.chatParticipantDao,
                    crossRefDao = db.chatParticipantsCrossRefDao
                )
            }
}