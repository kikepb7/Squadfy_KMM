package com.kikepb.club.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClubMemberDTO(
    val id: String,
    val clubId: String,
    val userId: String,
    val username: String,
    val email: String,
    val shirtNumber: Int? = null,
    val position: String? = null,
    val goalsScored: Int,
    val assists: Int,
    val yellowCards: Int,
    val redCards: Int,
    val minutesPlayed: Int,
    val matchesPlayed: Int,
    val role: String
)