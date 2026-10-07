package com.kikepb.core.data.auth.dto

import kotlinx.serialization.Serializable

/** Public profile from `GET /users/{userId}` (`ChatParticipantDto` in BACKEND.md §9). */
@Serializable
data class PublicUserSerializableDTO(
    val userId: String,
    val username: String,
    val profilePictureUrl: String? = null
)
