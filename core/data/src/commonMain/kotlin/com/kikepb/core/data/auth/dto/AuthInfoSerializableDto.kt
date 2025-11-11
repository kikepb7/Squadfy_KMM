package com.kikepb.core.data.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthInfoSerializableDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserSerializableDto
)
