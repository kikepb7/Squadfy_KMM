package com.kikepb.club.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateClubRequestDTO(
    val name: String,
    val description: String? = null,
    val clubLogoUrl: String? = null,
    val maxMembers: Int? = null
)
