package com.kikepb.domain.usecase

import com.kikepb.core.domain.auth.repository.AuthRepository

class ResetPasswordUseCase(private val authRepository: AuthRepository) {
    suspend fun resetPassword(newPassword: String, token: String) =
        authRepository.resetPassword(newPassword = newPassword, token = token)
}