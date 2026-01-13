package com.kikepb.core.data.auth

import com.kikepb.core.data.auth.dto.AuthInfoSerializableDTO
import com.kikepb.core.data.auth.dto.request.ChangePasswordRequestDTO
import com.kikepb.core.data.auth.dto.request.EmailRequestDTO
import com.kikepb.core.data.auth.dto.request.LoginRequestDTO
import com.kikepb.core.data.auth.dto.request.RefreshRequestDTO
import com.kikepb.core.data.auth.dto.request.RegisterRequestDTO
import com.kikepb.core.data.auth.dto.request.ResetPasswordRequestDTO
import com.kikepb.core.data.auth.provider.AuthRoutes.FORGOT_PASSWORD_ROUTE
import com.kikepb.core.data.auth.provider.AuthRoutes.LOGIN_ROUTE
import com.kikepb.core.data.auth.provider.AuthRoutes.REGISTER_ROUTE
import com.kikepb.core.data.auth.provider.AuthRoutes.RESEND_VERIFICATION_ROUTE
import com.kikepb.core.data.auth.provider.AuthRoutes.RESET_PASSWORD_ROUTE
import com.kikepb.core.data.auth.provider.AuthRoutes.VERIFY_EMAIL_ROUTE
import com.kikepb.core.data.mappers.toDomain
import com.kikepb.core.data.networking.get
import com.kikepb.core.data.networking.post
import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.repository.AuthRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.core.domain.util.map
import com.kikepb.core.domain.util.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider

class KtorAuthRepositoryImpl(
    private val httpClient: HttpClient
): AuthRepository {

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthInfoModel, DataError.Remote> =
        httpClient.post<LoginRequestDTO, AuthInfoSerializableDTO>(
            route = LOGIN_ROUTE,
            body = LoginRequestDTO(
                email = email,
                password = password
            )
        ).map { authInfoSerializable ->
            authInfoSerializable.toDomain()
        }

    override suspend fun register(
        username: String,
        email: String,
        password: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = REGISTER_ROUTE,
            body = RegisterRequestDTO(
                username = username,
                email = email,
                password = password
            )
        )
    }

    override suspend fun resendVerificationEmail(email: String): EmptyResult<DataError.Remote> =
        httpClient.post(
            route = RESEND_VERIFICATION_ROUTE,
            body = EmailRequestDTO(email = email)
        )

    override suspend fun verifyEmail(token: String): EmptyResult<DataError.Remote> =
        httpClient.get(
            route = VERIFY_EMAIL_ROUTE,
            queryParams = mapOf("token" to token)
        )

    override suspend fun forgotPassword(email: String): EmptyResult<DataError.Remote> =
        httpClient.post<EmailRequestDTO, Unit>(
            route = FORGOT_PASSWORD_ROUTE,
            body = EmailRequestDTO(email = email)
        )

    override suspend fun resetPassword(newPassword: String, token: String): EmptyResult<DataError.Remote> =
        httpClient.post(
            route = RESET_PASSWORD_ROUTE,
            body = ResetPasswordRequestDTO(newPassword = newPassword, token = token)
        )

    override suspend fun changePassword(currentPassword: String, newPassword: String): EmptyResult<DataError.Remote> =
        httpClient.post(
            route = "/auth/change-password",
            body = ChangePasswordRequestDTO(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
        )

    override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> =
        httpClient.post<RefreshRequestDTO, Unit>(
            route = "/auth/logout",
            body = RefreshRequestDTO(refreshToken = refreshToken)
        ).onSuccess {
            httpClient.authProvider<BearerAuthProvider>()?.clearToken()
        }
}