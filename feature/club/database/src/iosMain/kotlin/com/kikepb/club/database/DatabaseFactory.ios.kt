@file:OptIn(ExperimentalForeignApi::class)

package com.kikepb.club.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class DatabaseFactory {
    actual fun create(): RoomDatabase.Builder<SquadfyClubDatabase> {
        val dbFile = documentDirectory() + "/${SquadfyClubDatabase.DB_NAME}"
        return Room.databaseBuilder(name = dbFile)
    }

    private fun documentDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        return requireNotNull(value = documentDirectory?.path)
    }
}
