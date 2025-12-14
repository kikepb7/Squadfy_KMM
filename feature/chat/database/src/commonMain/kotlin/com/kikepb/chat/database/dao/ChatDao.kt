package com.kikepb.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.kikepb.chat.database.entities.ChatEntity
import com.kikepb.chat.database.entities.ChatInfoEntity
import com.kikepb.chat.database.entities.ChatMessageEntity
import com.kikepb.chat.database.entities.ChatParticipantCrossRef
import com.kikepb.chat.database.entities.ChatParticipantEntity
import com.kikepb.chat.database.entities.ChatWithParticipants
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT chatId FROM chatentity")
    suspend fun getAllChatIds(): List<String>

    @Query("SELECT * FROM chatentity ORDER BY lastActivityAt DESC")
    @Transaction
    fun getChatsWithParticipants(): Flow<List<ChatWithParticipants>>

    @Query("""
        SELECT DISTINCT c.*
        FROM chatentity c
        JOIN chatparticipantcrossref cpcr ON c.chatId = cpcr.chatId
            WHERE cpcr.isActive = 1
            ORDER BY lastActivityAt DESC
    """)
    @Transaction
    fun getChatsWithActiveParticipants(): Flow<List<ChatWithParticipants>>

    @Query("SELECT * FROM chatentity WHERE chatId = :id")
    @Transaction
    suspend fun getChatById(id: String): ChatWithParticipants?

    @Query("SELECT COUNT(*) FROM chatentity")
    fun getChatCount(): Flow<Int>

    @Query("""
        SELECT p.*
        FROM chatparticipantentity p
        JOIN chatparticipantcrossref cpcr ON p.userId = cpcr.userId
        WHERE cpcr.chatId = :chatId AND cpcr.isActive = true
        ORDER BY p.username
    """)
    fun getActiveParticipantsByChatId(chatId: String): Flow<List<ChatParticipantEntity>>

    @Query("SELECT * FROM chatentity WHERE chatId = :chatId")
    @Transaction
    fun getChatInfoById(chatId: String): Flow<ChatInfoEntity?>

    @Query("DELETE FROM chatentity")
    suspend fun deleteAllChats()

    @Query("DELETE FROM chatentity WHERE chatId = :chatId")
    suspend fun deleteChatById(chatId: String)

    @Transaction
    suspend fun deleteChatsByIds(chatIds: List<String>) {
        chatIds.forEach { chatId ->
            deleteChatById(chatId = chatId)
        }
    }

    @Upsert
    suspend fun upsertChat(chat: ChatEntity)

    @Upsert
    suspend fun upsertChats(chats: List<ChatEntity>)

    @Transaction
    suspend fun upsertChatWithParticipantsAndCrossRefs(
        chat: ChatEntity,
        participants: List<ChatParticipantEntity>,
        participantDao: ChatParticipantDao,
        crossRefDao: ChatParticipantsCrossRefDao
    ) {
        val crossRefs = participants.map { participant ->
            ChatParticipantCrossRef(
                chatId = chat.chatId,
                userId = participant.userId,
                isActive = true
            )
        }

        upsertChat(chat = chat)
        participantDao.upsertParticipants(participants = participants)
        crossRefDao.upsertCrossRefs(crossRef = crossRefs)
        crossRefDao.syncChatParticipants(chatId = chat.chatId, participants = participants)
    }

    @Transaction
    suspend fun upsertChatsWithParticipantsAndCrossRefs(
        chats: List<ChatWithParticipants>,
        participantDao: ChatParticipantDao,
        crossRefDao: ChatParticipantsCrossRefDao,
        messageDao: ChatMessageDao
    ) {
        val localChatIds = getAllChatIds()
        val allParticipants = chats.flatMap { it.participants }
        val allCrossRefs = chats.flatMap { chatWithParticipants ->
            chatWithParticipants.participants.map { participant ->
                ChatParticipantCrossRef(
                    chatId = chatWithParticipants.chat.chatId,
                    userId = participant.userId,
                    isActive = true
                )
            }
        }
        val serverChatIds = chats.map { it.chat.chatId }
        val staleChatIds = localChatIds - serverChatIds

        upsertChats(chats = chats.map { it.chat })

        chats.forEach { chat ->
            chat.lastMessage?.run {
                messageDao.upsertMessage(
                    message = ChatMessageEntity(
                        messageId = messageId,
                        chatId = chatId,
                        senderId = senderId,
                        content = content,
                        timestamp = timestamp,
                        deliveryStatus = deliveryStatus
                    )
                )
            }
        }

        participantDao.upsertParticipants(participants = allParticipants)
        crossRefDao.upsertCrossRefs(crossRef = allCrossRefs)

        chats.forEach { chat ->
            crossRefDao.syncChatParticipants(chatId = chat.chat.chatId, participants = chat.participants)
        }

        deleteChatsByIds(chatIds = staleChatIds)
    }
}