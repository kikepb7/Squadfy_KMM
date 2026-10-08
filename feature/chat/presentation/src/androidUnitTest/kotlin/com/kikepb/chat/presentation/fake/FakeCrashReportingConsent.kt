package com.kikepb.chat.presentation.fake

import com.kikepb.core.domain.crash.CrashReportingConsent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCrashReportingConsent(override val isAvailable: Boolean = true) : CrashReportingConsent {
    val enabled = MutableStateFlow(false)
    var appliedCount = 0
        private set

    override fun observe(): Flow<Boolean> = enabled
    override suspend fun set(enabled: Boolean) { this.enabled.value = enabled }
    override suspend fun applyStored() { appliedCount++ }
}
