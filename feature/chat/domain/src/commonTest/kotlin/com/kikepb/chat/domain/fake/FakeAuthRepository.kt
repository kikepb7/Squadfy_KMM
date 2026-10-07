package com.kikepb.chat.domain.fake

import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.UserModel
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result

class FakeAuthRepository : AuthRepository {

    var loginResult: Result<AuthInfoModel, DataError.Remote> = Result.Success(defaultAuthInfoModel())
    var registerResult: EmptyResult<DataError.Remote> = Result.Success(Unit)
    var resendVerificationResult: EmptyResult<DataError.Remote> = Result.Success(Unit)
    var verifyEmailResult: EmptyResult<DataError.Remote> = Result.Success(Unit)
    var forgotPasswordResult: EmptyResult<DataError.Remote> = Result.Success(Unit)
    var resetPasswordResult: EmptyResult<DataError.Remote> = Result.Success(Unit)
    var changePasswordResult: EmptyResult<DataError.Remote> = Result.Success(Unit)
    var logoutResult: EmptyResult<DataError.Remote> = Result.Success(Unit)

    var lastLogoutRefreshToken: String? = null

    override suspend fun login(email: String, password: String) = loginResult
    override suspend fun register(username: String, email: String, password: String) = registerResult
    override suspend fun resendVerificationEmail(email: String) = resendVerificationResult
    override suspend fun verifyEmail(token: String) = verifyEmailResult
    override suspend fun forgotPassword(email: String) = forgotPasswordResult
    override suspend fun resetPassword(newPassword: String, token: String) = resetPasswordResult
    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): EmptyResult<DataError.Remote> = changePasswordResult

    override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> {
        lastLogoutRefreshToken = refreshToken
        return logoutResult
    }

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
