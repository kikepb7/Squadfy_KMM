package com.kikepb.core.data.auth.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String,
)
