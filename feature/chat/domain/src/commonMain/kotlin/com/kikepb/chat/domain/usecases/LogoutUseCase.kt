package com.kikepb.chat.domain.usecases

import com.kikepb.core.domain.auth.repository.AuthRepository

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend fun logout(refreshToken: String) = authRepository.logout(refreshToken = refreshToken)
}