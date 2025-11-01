package com.kikepb.domain.usecase

import com.kikepb.core.domain.auth.AuthRepository

class AuthRegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend fun authRegister(username: String, email: String, password: String) =
        authRepository.register(username = username, email = email, password = password)
}