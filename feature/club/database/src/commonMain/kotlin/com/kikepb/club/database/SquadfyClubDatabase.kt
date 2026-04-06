package com.kikepb.club.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import com.kikepb.club.database.dao.ClubDao
import com.kikepb.club.database.dao.ClubMemberDao
import com.kikepb.club.database.entity.ClubEntity
import com.kikepb.club.database.entity.ClubMemberEntity

@Database(
    entities = [
        ClubEntity::class,
        ClubMemberEntity::class
    ],
    version = 1
)
@ConstructedBy(SquadfyClubDatabaseConstructor::class)
abstract class SquadfyClubDatabase : RoomDatabase() {
    abstract val clubDao: ClubDao
    abstract val clubMemberDao: ClubMemberDao

    companion object {
        const val DB_NAME = "squadfy_club.db"
    }
}
