package com.kikepb.chat.presentation.fake

import com.kikepb.core.domain.analytics.AnalyticsConsent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAnalyticsConsent(override val isAvailable: Boolean = true) : AnalyticsConsent {
    val enabled = MutableStateFlow(false)

    override fun observe(): Flow<Boolean> = enabled
    override suspend fun set(enabled: Boolean) { this.enabled.value = enabled }
    override suspend fun applyStored() = Unit
}
