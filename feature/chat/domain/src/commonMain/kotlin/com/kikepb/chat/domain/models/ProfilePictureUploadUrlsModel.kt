package com.kikepb.chat.domain.models

data class ProfilePictureUploadUrlsModel(
    val uploadUrl: String,
    val publicUrl: String,
    val headers: Map<String, String>
)
