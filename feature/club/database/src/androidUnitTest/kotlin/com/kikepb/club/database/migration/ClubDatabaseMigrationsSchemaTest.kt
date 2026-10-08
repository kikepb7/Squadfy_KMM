package com.kikepb.club.database.migration

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Room validates the migrated tables against the compiled schema when the database opens. This test
 * keeps the hand-written migration SQL identical to the exported v3 schema (AC-003-15), so a mismatch
 * fails here instead of crashing on a device.
 */
class ClubDatabaseMigrationsSchemaTest {

    private fun schema(version: Int) = Json.parseToJsonElement(
        File("schemas/com.kikepb.club.database.SquadfyClubDatabase/$version.json").readText()
    ).jsonObject.getValue("database").jsonObject

    private val schema = schema(version = 3)

    private fun entity(table: String, version: Int = 3) = schema(version).getValue("entities").jsonArray
        .map { it.jsonObject }
        .first { it.getValue("tableName").jsonPrimitive.content == table }

    private fun createSql(table: String, version: Int = 3) =
        entity(table, version).getValue("createSql").jsonPrimitive.content.replace("\${TABLE_NAME}", table)

    @Test
    fun `AC-003-15 migration creates the club table exactly as schema v3`() {
        assertEquals(createSql("club"), ClubDatabaseMigrations.CREATE_CLUB_V3)
    }

    @Test
    fun `AC-003-15 migration creates the club_member table and index exactly as schema v3`() {
        assertEquals(createSql("club_member"), ClubDatabaseMigrations.CREATE_CLUB_MEMBER_V3)
        val index = entity("club_member").getValue("indices").jsonArray.single().jsonObject
            .getValue("createSql").jsonPrimitive.content.replace("\${TABLE_NAME}", "club_member")
        assertEquals(index, ClubDatabaseMigrations.CREATE_CLUB_MEMBER_INDEX_V3)
    }

    @Test
    fun `AC-003-15 every old version has a migration path to the current schema`() {
        assertEquals(listOf(1 to 3, 2 to 3, 3 to 4), ClubDatabaseMigrations.ALL.map { it.startVersion to it.endVersion })
    }

    @Test
    fun `AC-015-05 v3 plus the added clubPictureUrl column is exactly schema v4`() {
        // SQLite appends ADD COLUMN at the end of the column list, before the table constraints
        val v3PlusColumn = ClubDatabaseMigrations.CREATE_CLUB_MEMBER_V3
            .replace("`role` TEXT NOT NULL, ", "`role` TEXT NOT NULL, `clubPictureUrl` TEXT, ")
        assertEquals(createSql("club_member", version = 4), v3PlusColumn)
        assertEquals("ALTER TABLE `club_member` ADD COLUMN `clubPictureUrl` TEXT", ClubDatabaseMigrations.ADD_CLUB_PICTURE_V4)
        assertEquals(createSql("club", version = 3), createSql("club", version = 4))
    }
}
