package com.kikepb.core.data.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserSerializableDTO(
    val id: String,
    val email: String,
    val username: String,
    val hasVerifiedEmail: Boolean,
    val profilePictureUrl: String? = null
)