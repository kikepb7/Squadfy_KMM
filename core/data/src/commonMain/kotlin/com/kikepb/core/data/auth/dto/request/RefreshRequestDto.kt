package com.kikepb.core.data.auth.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequestDto(
    val refreshToken: String
)