package com.kikepb.club.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MatchSignupDTO(
    val id: String,
    val matchId: String,
    val clubMemberId: String? = null,
    val guestName: String? = null,
    val position: String? = null,
    val rating: Int? = null,
    val status: String,
    val signedUpAt: String
)
