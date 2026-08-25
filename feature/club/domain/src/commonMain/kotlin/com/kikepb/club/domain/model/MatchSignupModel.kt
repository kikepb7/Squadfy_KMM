package com.kikepb.club.domain.model

data class MatchSignupModel(
    val id: String,
    val matchId: String,
    val clubMemberId: String?,
    val guestName: String?,
    val position: String?,
    val rating: Int?,
    val status: String,
    val signedUpAt: String
)
