package com.kikepb.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.kikepb.chat.database.entities.ChatParticipantCrossRef
import com.kikepb.chat.database.entities.ChatParticipantEntity

@Dao
interface ChatParticipantsCrossRefDao {

    @Query("SELECT userId FROM chatparticipantcrossref WHERE chatId = :chatId")
    suspend fun getActiveParticipantIdsByChat(chatId: String): List<String>

    @Query("SELECT userId FROM chatparticipantcrossref WHERE chatId = :chatId")
    suspend fun getAllParticipantIdsByChat(chatId: String): List<String>

    @Query("""
        UPDATE chatparticipantcrossref
        SET isActive = 0
        WHERE chatId = :chatId AND userId IN (:userIds)
    """)
    suspend fun markParticipantsAsInactive(chatId: String, userIds: List<String>)

    @Query("""
        UPDATE chatparticipantcrossref
        SET isActive = 1
        WHERE chatId = :chatId AND userId IN (:userIds)
    """)
    suspend fun reactivateParticipants(chatId: String, userIds: List<String>)

    @Upsert
    suspend fun upsertCrossRefs(crossRef: List<ChatParticipantCrossRef>)

    @Transaction
    suspend fun syncChatParticipants(
        chatId: String,
        participants: List<ChatParticipantEntity>
    ) {
        if (participants.isEmpty()) return

        val serverParticipantsIds = participants.map { it.userId }.toSet()
        val allLocalParticipantsIds = getAllParticipantIdsByChat(chatId = chatId).toSet()
        val activeLocalParticipantIds = getActiveParticipantIdsByChat(chatId = chatId).toSet()
        val inactiveLocalParticipantIds = allLocalParticipantsIds - activeLocalParticipantIds

        val participantsToReactivate = serverParticipantsIds.intersect(other = inactiveLocalParticipantIds)
        val participantsToDeactivate = activeLocalParticipantIds - serverParticipantsIds

        reactivateParticipants(chatId = chatId, userIds = participantsToReactivate.toList())
        markParticipantsAsInactive(chatId = chatId, userIds = participantsToDeactivate.toList())

        val completelyNewParticipantIds = serverParticipantsIds - allLocalParticipantsIds
        val newCrossRefs = completelyNewParticipantIds.map { userId ->
            ChatParticipantCrossRef(
                chatId = chatId,
                userId = userId,
                isActive = true
            )
        }
        upsertCrossRefs(crossRef = newCrossRefs)
    }
}