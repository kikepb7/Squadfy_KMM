package com.kikepb.core.domain.auth

import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult

interface AuthRepository {
    suspend fun register(username: String, email: String, password: String): EmptyResult<DataError.Remote>
}