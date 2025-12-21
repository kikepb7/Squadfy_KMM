package com.kikepb.chat.data.datasource.local

import com.kikepb.chat.data.mappers.toDomain
import com.kikepb.chat.data.mappers.toEntity
import com.kikepb.chat.data.mappers.toLastMessageView
import com.kikepb.chat.database.SquadfyChatDatabase
import com.kikepb.chat.database.entities.ChatInfoEntity
import com.kikepb.chat.database.entities.ChatParticipantEntity
import com.kikepb.chat.database.entities.ChatWithParticipants
import com.kikepb.chat.domain.models.ChatInfoModel
import com.kikepb.chat.domain.models.ChatModel
import com.kikepb.chat.domain.models.ChatParticipantModel
import com.kikepb.chat.domain.repository.ChatRepository
import com.kikepb.chat.domain.repository.ChatService
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.asEmptyResult
import com.kikepb.core.domain.util.onSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope

class OfflineFirstChatRepositoryImpl(
    private val chatService: ChatService,
    private val db: SquadfyChatDatabase
): ChatRepository {

    override fun getChats(): Flow<List<ChatModel>> =
        db.chatDao.getChatsWithParticipants()
            .map { allChatsWithParticipants ->
                supervisorScope {
                    allChatsWithParticipants.map { chatWithParticipants ->
                        async {
                            ChatWithParticipants(
                                chat = chatWithParticipants.chat,
                                participants = chatWithParticipants.participants
                                    .onlyActive(chatId = chatWithParticipants.chat.chatId),
                                lastMessage = chatWithParticipants.lastMessage
                            )
                        }
                    }
                        .awaitAll()
                        .map { it.toDomain() }
                }
            }

    override fun getChatInfoById(chatId: String): Flow<ChatInfoModel> =
        db.chatDao.getChatInfoById(chatId = chatId)
            .filterNotNull()
            .map { chatInfo ->
                ChatInfoEntity(
                    chat = chatInfo.chat,
                    participants = chatInfo.participants.onlyActive(chatId = chatInfo.chat.chatId),
                    messagesWithSenders = chatInfo.messagesWithSenders
                )
            }
            .map { it.toDomain() }

    override fun getActiveParticipantsByChatId(chatId: String): Flow<List<ChatParticipantModel>> =
        db.chatDao.getActiveParticipantsByChatId(chatId = chatId)
            .map { participants ->
                participants.map { it.toDomain() }
            }

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

    override suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote> =
        chatService.leaveChat(chatId = chatId)
            .onSuccess {
                db.chatDao.deleteChatById(chatId = chatId)
            }

    private suspend fun List<ChatParticipantEntity>.onlyActive(chatId: String): List<ChatParticipantEntity> {
        val activeParticipants = db.chatDao.getActiveParticipantsByChatId(chatId = chatId)
            .first()
            .map { it.userId }

        return this.filter { it.userId in activeParticipants }
    }

    override suspend fun addParticipantsToChat(
        chatId: String,
        userIds: List<String>
    ): Result<ChatModel, DataError.Remote> =
        chatService.addParticipantsToChat(chatId = chatId, userIds = userIds)
            .onSuccess { chat ->
                db.chatDao.upsertChatWithParticipantsAndCrossRefs(
                    chat = chat.toEntity(),
                    participants = chat.participants.map { it.toEntity() },
                    participantDao = db.chatParticipantDao,
                    crossRefDao = db.chatParticipantsCrossRefDao
                )
            }
}