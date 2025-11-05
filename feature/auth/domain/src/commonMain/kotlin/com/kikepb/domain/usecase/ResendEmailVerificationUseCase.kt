package com.kikepb.domain.usecase

import com.kikepb.core.domain.auth.AuthRepository

class ResendEmailVerificationUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String) =
        authRepository.resendVerificationEmail(email)
}