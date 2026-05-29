package com.kikepb.auth.presentation.fake

import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.repository.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSessionStorage : SessionStorage {

    private val authInfoFlow = MutableStateFlow<AuthInfoModel?>(null)

    var savedInfo: AuthInfoModel? = null
        private set

    override fun observeAuthInfo(): Flow<AuthInfoModel?> = authInfoFlow

    override suspend fun set(info: AuthInfoModel?) {
        savedInfo = info
        authInfoFlow.value = info
    }
}
