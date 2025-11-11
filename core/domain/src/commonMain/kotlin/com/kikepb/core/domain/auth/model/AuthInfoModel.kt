package com.kikepb.core.domain.auth.model

data class AuthInfoModel(
    val accessToken: String,
    val refreshToken: String,
    val user: UserModel
)
