package com.kikepb.club.domain.model

data class ClubMatchModel(
    val id: String,
    val clubId: String,
    val scheduledAt: String,
    val signupOpensAt: String,
    val signupClosesAt: String,
    val status: String,
    val teamAScore: Int?,
    val teamBScore: Int?,
    val teamA: List<MatchParticipantModel>,
    val teamB: List<MatchParticipantModel>
)
