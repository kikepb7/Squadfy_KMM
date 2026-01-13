package com.kikepb.chat.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceTokenRequestDTO(
    val token: String,
    val platform: String
)
