package com.kikepb.club.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClubMatchDTO(
    val id: String,
    val clubId: String,
    val scheduledAt: String,
    val signupOpensAt: String,
    val signupClosesAt: String,
    val status: String,
    val teamAScore: Int? = null,
    val teamBScore: Int? = null,
    val teamA: List<MatchParticipantDTO> = emptyList(),
    val teamB: List<MatchParticipantDTO> = emptyList()
)
