package com.kikepb.chat.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ProfilePictureUploadUrlsResponseDTO(
    val uploadUrl: String,
    val publicUrl: String,
    val headers: Map<String, String>
)
