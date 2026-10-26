package com.kikepb.core.firebase.auth

import kotlinx.serialization.Serializable

/** `users/{uid}`: the public profile (no email, constitution IV.3). Written by `completeProfile` and the app. */
@Serializable
internal data class UserProfileDto(
    val username: String? = null,
    val usernameLower: String? = null,
    val pictureUrl: String? = null
)

@Serializable
internal data class CompleteProfileRequest(val username: String)
