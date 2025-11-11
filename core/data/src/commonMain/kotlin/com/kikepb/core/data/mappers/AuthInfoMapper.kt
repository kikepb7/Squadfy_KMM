package com.kikepb.core.data.mappers

import com.kikepb.core.data.auth.dto.AuthInfoSerializableDto
import com.kikepb.core.data.auth.dto.UserSerializableDto
import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.UserModel

fun AuthInfoSerializableDto.toDomain(): AuthInfoModel {
    return AuthInfoModel(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = user.toDomain()
    )
}

fun UserSerializableDto.toDomain(): UserModel {
    return UserModel(
        id = id,
        email = email,
        username = username,
        hasVerifiedEmail = hasVerifiedEmail,
        profilePictureUrl = profilePictureUrl
    )
}