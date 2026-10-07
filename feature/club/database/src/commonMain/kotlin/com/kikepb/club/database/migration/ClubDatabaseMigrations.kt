package com.kikepb.club.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * `club` and `club_member` are a cache of the backend API v1 (spec 003). The v3 schema drops columns the
 * backend no longer sends (email, member stats, schedule fields), so pre-v3 caches are recreated empty with
 * the exact v3 SQL (`schemas/.../3.json`) and refilled on the next sync. The child table goes first so the
 * `ON DELETE CASCADE` foreign key never acts on half-migrated data.
 */
object ClubDatabaseMigrations {

    internal val CREATE_CLUB_V3 =
        "CREATE TABLE IF NOT EXISTS `club` (`clubId` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT, " +
            "`clubLogoUrl` TEXT, `ownerId` TEXT NOT NULL, `invitationCode` TEXT NOT NULL, `maxMembers` INTEGER, " +
            "`membersCount` INTEGER NOT NULL, PRIMARY KEY(`clubId`))"

    internal val CREATE_CLUB_MEMBER_V3 =
        "CREATE TABLE IF NOT EXISTS `club_member` (`memberId` TEXT NOT NULL, `clubId` TEXT NOT NULL, " +
            "`userId` TEXT NOT NULL, `username` TEXT NOT NULL, `shirtNumber` INTEGER, `profilePictureUrl` TEXT, " +
            "`position` TEXT, `role` TEXT NOT NULL, PRIMARY KEY(`memberId`), FOREIGN KEY(`clubId`) " +
            "REFERENCES `club`(`clubId`) ON UPDATE NO ACTION ON DELETE CASCADE )"

    internal val CREATE_CLUB_MEMBER_INDEX_V3 =
        "CREATE INDEX IF NOT EXISTS `index_club_member_clubId` ON `club_member` (`clubId`)"

    private fun recreateCacheTables(from: Int) = object : Migration(from, 3) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("DROP TABLE IF EXISTS `club_member`")
            connection.execSQL("DROP TABLE IF EXISTS `club`")
            connection.execSQL(CREATE_CLUB_V3)
            connection.execSQL(CREATE_CLUB_MEMBER_V3)
            connection.execSQL(CREATE_CLUB_MEMBER_INDEX_V3)
        }
    }

    val MIGRATION_1_3: Migration = recreateCacheTables(from = 1)
    val MIGRATION_2_3: Migration = recreateCacheTables(from = 2)

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_3, MIGRATION_2_3)
}
