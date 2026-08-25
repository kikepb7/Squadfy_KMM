package com.kikepb.club.domain.model

data class MatchParticipantModel(
    val signupId: String,
    val clubMemberId: String?,
    val displayName: String,
    val position: String?,
    val rating: Int
)
