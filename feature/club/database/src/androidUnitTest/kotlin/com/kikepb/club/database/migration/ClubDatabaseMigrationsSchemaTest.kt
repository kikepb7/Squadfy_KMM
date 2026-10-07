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

    private val schema = Json.parseToJsonElement(
        File("schemas/com.kikepb.club.database.SquadfyClubDatabase/3.json").readText()
    ).jsonObject.getValue("database").jsonObject

    private fun entity(table: String) = schema.getValue("entities").jsonArray
        .map { it.jsonObject }
        .first { it.getValue("tableName").jsonPrimitive.content == table }

    private fun createSql(table: String) =
        entity(table).getValue("createSql").jsonPrimitive.content.replace("\${TABLE_NAME}", table)

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
    fun `AC-003-15 every pre-v3 version has a migration path`() {
        assertEquals(listOf(1 to 3, 2 to 3), ClubDatabaseMigrations.ALL.map { it.startVersion to it.endVersion })
    }
}
