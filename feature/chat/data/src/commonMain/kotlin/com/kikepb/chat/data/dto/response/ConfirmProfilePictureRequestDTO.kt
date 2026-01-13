package com.kikepb.chat.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmProfilePictureRequestDTO(
    val publicUrl: String
)
