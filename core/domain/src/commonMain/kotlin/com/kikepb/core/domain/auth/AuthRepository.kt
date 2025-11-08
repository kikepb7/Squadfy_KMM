package com.kikepb.core.domain.auth

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult

interface AuthRepository {
    suspend fun register(username: String, email: String, password: String): EmptyResult<DataError.Remote>
    suspend fun resendVerificationEmail(email: String): EmptyResult<DataError.Remote>
    suspend fun verifyEmail(token: String): EmptyResult<DataError.Remote>
}