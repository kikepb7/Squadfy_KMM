package com.kikepb.core.domain.auth.repository

import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthInfoModel, DataError.Remote>
    /** Success carries `hasVerifiedEmail`: true when the backend has email verification switched off (backend spec 009). */
    suspend fun register(username: String, email: String, password: String): Result<Boolean, DataError.Remote>
    suspend fun resendVerificationEmail(email: String): EmptyResult<DataError.Remote>
    suspend fun verifyEmail(token: String): EmptyResult<DataError.Remote>
    suspend fun forgotPassword(email: String): EmptyResult<DataError.Remote>
    suspend fun resetPassword(newPassword: String, token: String): EmptyResult<DataError.Remote>
    suspend fun changePassword(currentPassword: String, newPassword: String): EmptyResult<DataError.Remote>
    suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote>
}