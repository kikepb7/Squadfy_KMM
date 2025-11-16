package com.kikepb.core.data.auth.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequestDto(
    val newPassword: String,
    val token: String
)
