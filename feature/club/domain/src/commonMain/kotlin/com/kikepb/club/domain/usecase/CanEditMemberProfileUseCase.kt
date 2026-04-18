package com.kikepb.club.domain.usecase

import com.kikepb.core.domain.auth.repository.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CanEditMemberProfileUseCase(private val sessionStorage: SessionStorage) {
    operator fun invoke(memberUserId: String): Flow<Boolean> =
        sessionStorage.observeAuthInfo().map { authInfo -> authInfo?.user?.id == memberUserId }
}
