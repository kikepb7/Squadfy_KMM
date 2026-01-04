package com.kikepb.chat.data.mappers

import com.kikepb.chat.data.dto.response.ProfilePictureUploadUrlsResponseDTO
import com.kikepb.chat.domain.models.ProfilePictureUploadUrlsModel

fun ProfilePictureUploadUrlsResponseDTO.toDomain(): ProfilePictureUploadUrlsModel =
    ProfilePictureUploadUrlsModel(
        uploadUrl = uploadUrl,
        publicUrl = publicUrl,
        headers = headers
    )