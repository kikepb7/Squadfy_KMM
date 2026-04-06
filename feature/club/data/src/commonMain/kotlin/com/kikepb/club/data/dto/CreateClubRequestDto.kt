package com.kikepb.club.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateClubRequestDto(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("clubLogoUrl") val clubLogoUrl: String? = null,
    @SerialName("maxMembers") val maxMembers: Int? = null
)
