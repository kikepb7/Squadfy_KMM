package com.kikepb.chat.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import com.kikepb.chat.database.dao.ChatDao
import com.kikepb.chat.database.dao.ChatMessageDao
import com.kikepb.chat.database.dao.ChatParticipantDao
import com.kikepb.chat.database.dao.ChatParticipantsCrossRefDao
import com.kikepb.chat.database.entities.ChatEntity
import com.kikepb.chat.database.entities.ChatMessageEntity
import com.kikepb.chat.database.entities.ChatParticipantCrossRef
import com.kikepb.chat.database.entities.ChatParticipantEntity
import com.kikepb.chat.database.view.LastMessageView

@Database(
    entities = [
        ChatEntity::class,
        ChatParticipantEntity::class,
        ChatMessageEntity::class,
        ChatParticipantCrossRef::class
    ],
    views = [
        LastMessageView::class
    ],
    version = 1
)
@ConstructedBy(SquadfyChatDatabaseConstructor::class)
abstract class SquadfyChatDatabase: RoomDatabase() {
    abstract val chatDao: ChatDao
    abstract val chatParticipantDao: ChatParticipantDao
    abstract val chatMessageDao: ChatMessageDao
    abstract val chatParticipantsCrossRefDao: ChatParticipantsCrossRefDao

    companion object {
        const val DB_NAME = "squadfy.db"
    }
}