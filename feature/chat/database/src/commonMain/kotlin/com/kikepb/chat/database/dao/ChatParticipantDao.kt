package com.kikepb.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kikepb.chat.database.entities.ChatParticipantEntity

@Dao
interface ChatParticipantDao {

    @Query("SELECT * FROM chatparticipantentity")
    suspend fun getAllParticipants(): List<ChatParticipantEntity>

    @Upsert
    suspend fun upsertParticipant(participant: ChatParticipantEntity)

    @Query("""
        UPDATE chatparticipantentity
        SET profilePictureUrl = :newUrl
        WHERE userId = :userId
    """)
    suspend fun updateProfilePictureUrl(userId: String, newUrl: String?)

    @Upsert
    suspend fun upsertParticipants(participants: List<ChatParticipantEntity>)
}