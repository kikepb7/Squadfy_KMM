package com.kikepb.core.data.auth

import com.kikepb.core.data.auth.dto.request.EmailRequestDto
import com.kikepb.core.data.auth.dto.request.RegisterRequestDto
import com.kikepb.core.data.auth.provider.AuthUrlProvider
import com.kikepb.core.data.networking.get
import com.kikepb.core.data.networking.post
import com.kikepb.core.domain.auth.AuthRepository
import com.kikepb.core.domain.util.DataError
import com.kikepb.core.domain.util.EmptyResult
import io.ktor.client.HttpClient

class KtorAuthRepositoryImpl(
    private val httpClient: HttpClient
): AuthRepository {

    override suspend fun register(
        username: String,
        email: String,
        password: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = AuthUrlProvider.REGISTER_URL,
            body = RegisterRequestDto(
                username = username,
                email = email,
                password = password
            )
        )
    }

    override suspend fun resendVerificationEmail(email: String): EmptyResult<DataError.Remote> =
        httpClient.post(
            route = "/auth/resend-verification",
            body = EmailRequestDto(email = email)
        )

    override suspend fun verifyEmail(token: String): EmptyResult<DataError.Remote> =
        httpClient.get(
            route = "/auth/verify",
            queryParams = mapOf("token" to token)
        )
}