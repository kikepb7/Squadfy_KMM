package com.kikepb.club.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "club_member",
    foreignKeys = [
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["clubId"],
            childColumns = ["clubId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clubId")]
)
data class ClubMemberEntity(
    @PrimaryKey val memberId: String,
    val clubId: String,
    val userId: String,
    val username: String,
    val email: String,
    val shirtNumber: Int?,
    val position: String?,
    val goalsScored: Int,
    val assists: Int,
    val yellowCards: Int,
    val redCards: Int,
    val minutesPlayed: Int,
    val matchesPlayed: Int,
    val role: String
)
