package com.kikepb.domain.usecase

import com.kikepb.core.domain.auth.repository.AuthRepository

class AuthVerifyEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend fun verifyEmail(token: String) =
        authRepository.verifyEmail(token = token)
}