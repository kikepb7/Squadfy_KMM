package com.kikepb.auth.presentation.fake

import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.UserModel
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.Result.Success

class FakeAuthRepository : AuthRepository {

    var loginResult: Result<AuthInfoModel, DataError.Remote> = Success(defaultAuthInfoModel())
    var registerResult: EmptyResult<DataError.Remote> = Success(Unit)
    var resendVerificationResult: EmptyResult<DataError.Remote> = Success(Unit)
    var verifyEmailResult: EmptyResult<DataError.Remote> = Success(Unit)
    var forgotPasswordResult: EmptyResult<DataError.Remote> = Success(Unit)
    var resetPasswordResult: EmptyResult<DataError.Remote> = Success(Unit)

    override suspend fun login(email: String, password: String) = loginResult

    override suspend fun register(username: String, email: String, password: String) = registerResult

    override suspend fun resendVerificationEmail(email: String) = resendVerificationResult

    override suspend fun verifyEmail(token: String) = verifyEmailResult

    override suspend fun forgotPassword(email: String) = forgotPasswordResult

    override suspend fun resetPassword(newPassword: String, token: String) = resetPasswordResult

    override suspend fun changePassword(currentPassword: String, newPassword: String): EmptyResult<DataError.Remote> = Success(Unit)

    override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> = Success(Unit)

    companion object {
        fun defaultAuthInfoModel() = AuthInfoModel(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            user = UserModel(
                id = "user-id",
                email = "user@example.com",
                username = "testuser",
                hasVerifiedEmail = true,
                profilePictureUrl = null
            )
        )
    }
}
