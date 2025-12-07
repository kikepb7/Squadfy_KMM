package com.kikepb.chat.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    primaryKeys = ["chatId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["chatId"],
            childColumns = ["chatId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = ChatParticipantEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = CASCADE
        )
    ]
)
data class ChatParticipantCrossRef(
    val chatId: String,
    val userId: String,
    val isActive: Boolean
)
