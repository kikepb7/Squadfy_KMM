package com.kikepb.chat.presentation.fake

import com.kikepb.core.domain.featureflag.AppEnvironment
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.core.domain.featureflag.ResolvedFeatureFlag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

/** Every flag off unless switched on with [set]. */
class FakeFeatureFlags(vararg enabled: FeatureFlag) : FeatureFlags {
    private val enabledFlags = MutableStateFlow(enabled.toSet())
    override val environment = AppEnvironment.PRE
    override fun isEnabled(flag: FeatureFlag) = flag in enabledFlags.value
    override fun observe(flag: FeatureFlag): Flow<Boolean> = enabledFlags.map { flag in it }
    override fun observeAll(): Flow<List<ResolvedFeatureFlag>> = emptyFlow()
    fun set(flag: FeatureFlag, enabled: Boolean) {
        enabledFlags.value = if (enabled) enabledFlags.value + flag else enabledFlags.value - flag
    }
}
