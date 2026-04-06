package com.kikepb.club.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.kikepb.club.database.entity.ClubEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubDao {

    @Query("SELECT * FROM club ORDER BY name ASC")
    fun observeAllClubs(): Flow<List<ClubEntity>>

    @Query("SELECT * FROM club WHERE clubId = :clubId")
    fun observeClubById(clubId: String): Flow<ClubEntity?>

    @Query("SELECT clubId FROM club")
    suspend fun getAllClubIds(): List<String>

    @Upsert
    suspend fun upsertClub(club: ClubEntity)

    @Upsert
    suspend fun upsertClubs(clubs: List<ClubEntity>)

    @Query("DELETE FROM club WHERE clubId = :clubId")
    suspend fun deleteClubById(clubId: String)

    @Query("DELETE FROM club")
    suspend fun deleteAllClubs()

    @Transaction
    suspend fun syncClubs(clubs: List<ClubEntity>) {
        val localIds = getAllClubIds()
        val serverIds = clubs.map { it.clubId }.toSet()
        val staleIds = localIds.filter { it !in serverIds }
        upsertClubs(clubs)
        staleIds.forEach { deleteClubById(it) }
    }
}
