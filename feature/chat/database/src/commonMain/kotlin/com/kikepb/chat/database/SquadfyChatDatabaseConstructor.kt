package com.kikepb.chat.database

import androidx.room.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
expect object SquadfyChatDatabaseConstructor: RoomDatabaseConstructor<SquadfyChatDatabase> {
    override fun initialize(): SquadfyChatDatabase
}