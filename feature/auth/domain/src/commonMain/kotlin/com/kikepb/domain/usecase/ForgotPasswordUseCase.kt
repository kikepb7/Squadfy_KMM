package com.kikepb.domain.usecase

import com.kikepb.core.domain.auth.repository.AuthRepository

class ForgotPasswordUseCase(private val authRepository: AuthRepository) {
    suspend fun forgotPassword(email: String) =
        authRepository.forgotPassword(email = email)
}