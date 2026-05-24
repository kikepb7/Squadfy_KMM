package com.kikepb.globalPosition.domain.usecase

import com.kikepb.core.domain.auth.model.UserModel
import com.kikepb.core.domain.auth.repository.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetCurrentUserUseCase(private val sessionStorage: SessionStorage) {
    operator fun invoke(): Flow<UserModel?> = sessionStorage.observeAuthInfo().map { it?.user }
}
