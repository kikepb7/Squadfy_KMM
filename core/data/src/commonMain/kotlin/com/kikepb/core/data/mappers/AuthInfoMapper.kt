package com.kikepb.core.data.mappers

import com.kikepb.core.data.auth.dto.AuthInfoSerializableDTO
import com.kikepb.core.data.auth.dto.UserSerializableDTO
import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.UserModel

fun AuthInfoSerializableDTO.toDomain(): AuthInfoModel {
    return AuthInfoModel(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = user.toDomain()
    )
}

fun UserSerializableDTO.toDomain(): UserModel {
    return UserModel(
        id = id,
        email = email,
        username = username,
        hasVerifiedEmail = hasVerifiedEmail,
        profilePictureUrl = profilePictureUrl
    )
}

fun UserModel.toDto(): UserSerializableDTO {
    return UserSerializableDTO(
        id = id,
        email = email,
        username = username,
        hasVerifiedEmail = hasVerifiedEmail,
        profilePictureUrl = profilePictureUrl
    )
}

fun AuthInfoModel.toDto(): AuthInfoSerializableDTO {
    return AuthInfoSerializableDTO(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = user.toDto()
    )
}