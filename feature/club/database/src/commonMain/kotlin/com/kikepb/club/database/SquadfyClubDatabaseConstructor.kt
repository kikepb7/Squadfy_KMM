package com.kikepb.club.database

import androidx.room.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
expect object SquadfyClubDatabaseConstructor : RoomDatabaseConstructor<SquadfyClubDatabase> {
    override fun initialize(): SquadfyClubDatabase
}
