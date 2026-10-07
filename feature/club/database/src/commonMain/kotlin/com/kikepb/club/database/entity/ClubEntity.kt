package com.kikepb.club.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "club")
data class ClubEntity(
    @PrimaryKey val clubId: String,
    val name: String,
    val description: String?,
    val clubLogoUrl: String?,
    val ownerId: String,
    val invitationCode: String,
    val maxMembers: Int?,
    val membersCount: Int
)
