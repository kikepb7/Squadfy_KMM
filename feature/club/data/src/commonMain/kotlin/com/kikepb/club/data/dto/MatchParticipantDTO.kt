package com.kikepb.club.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MatchParticipantDTO(
    val signupId: String,
    val clubMemberId: String? = null,
    val displayName: String,
    val position: String? = null,
    val rating: Int
)
