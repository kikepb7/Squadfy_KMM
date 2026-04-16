package com.kikepb.club.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClubLogoUploadUrlsResponseDTO(
    val uploadUrl: String,
    val publicUrl: String,
    val headers: Map<String, String>
)
