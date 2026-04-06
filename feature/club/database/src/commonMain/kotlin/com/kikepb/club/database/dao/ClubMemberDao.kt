package com.kikepb.club.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.kikepb.club.database.entity.ClubMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubMemberDao {

    @Query("SELECT * FROM club_member WHERE clubId = :clubId ORDER BY username ASC")
    fun observeMembersByClub(clubId: String): Flow<List<ClubMemberEntity>>

    @Upsert
    suspend fun upsertMembers(members: List<ClubMemberEntity>)

    @Query("DELETE FROM club_member WHERE clubId = :clubId")
    suspend fun deleteMembersByClub(clubId: String)

    @Query("DELETE FROM club_member")
    suspend fun deleteAllMembers()

    @Transaction
    suspend fun syncMembers(clubId: String, members: List<ClubMemberEntity>) {
        deleteMembersByClub(clubId = clubId)
        upsertMembers(members = members)
    }
}
