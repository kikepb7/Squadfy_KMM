package com.kikepb.domain.usecase

import com.kikepb.core.domain.auth.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend fun login(email: String, password: String) =
        authRepository.login(email = email, password = password)
}