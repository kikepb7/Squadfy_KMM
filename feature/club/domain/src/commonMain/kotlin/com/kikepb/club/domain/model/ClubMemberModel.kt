package com.kikepb.club.domain.model

data class ClubMemberModel(
    val id: String,
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
