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
    val shirtNumber: Int?,
    val profilePictureUrl: String?,
    /** `PlayerPosition` name or null. */
    val position: String?,
    /** `ClubMemberRole` name. */
    val role: String,
    /** Spec 015 (backend spec 012 RN-C), added in schema v4. */
    val clubPictureUrl: String? = null
)
