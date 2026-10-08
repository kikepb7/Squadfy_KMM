package com.kikepb.core.data.auth.dto.request

import kotlinx.serialization.Serializable

/** Body of `DELETE /me`: the password confirms the deletion (backend spec 010 RN-A1). */
@Serializable
data class DeleteAccountRequestDTO(
    val password: String
)
