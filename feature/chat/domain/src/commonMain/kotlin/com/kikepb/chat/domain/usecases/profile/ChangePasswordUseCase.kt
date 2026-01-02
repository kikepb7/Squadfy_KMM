package com.kikepb.chat.domain.usecases.profile

import com.kikepb.core.domain.auth.repository.AuthRepository

class ChangePasswordUseCase(private val authRepository: AuthRepository) {
    suspend fun changePassword(currentPassword: String,newPassword: String) = authRepository.changePassword(currentPassword = currentPassword, newPassword = newPassword)
}