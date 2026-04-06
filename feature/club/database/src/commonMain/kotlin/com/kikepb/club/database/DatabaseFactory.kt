package com.kikepb.club.database

import androidx.room.RoomDatabase

expect class DatabaseFactory {
    fun create(): RoomDatabase.Builder<SquadfyClubDatabase>
}
