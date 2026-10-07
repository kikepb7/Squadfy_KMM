package com.kikepb.club.presentation.fake

import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.UserModel
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.featureflag.AppEnvironment
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FeatureFlagResolver
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.core.domain.featureflag.ResolvedFeatureFlag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSessionStorage(userId: String = "me") : SessionStorage {
    private val info = MutableStateFlow<AuthInfoModel?>(
        AuthInfoModel("a", "r", UserModel(id = userId, email = "e", username = userId, hasVerifiedEmail = true, profilePictureUrl = null))
    )
    override fun observeAuthInfo(): Flow<AuthInfoModel?> = info
    override suspend fun set(info: AuthInfoModel?) { this.info.value = info }
}

/** Feature flags driven by explicit overrides on top of the PRE defaults. */
class FakeFeatureFlags(vararg enabled: Pair<FeatureFlag, Boolean>) : FeatureFlags {
    val overrides = MutableStateFlow(enabled.associate { it.first.key to it.second })
    override val environment = AppEnvironment.PRE
    override fun isEnabled(flag: FeatureFlag) = FeatureFlagResolver.resolve(flag, environment, emptyMap(), overrides.value).enabled
    override fun observe(flag: FeatureFlag): Flow<Boolean> = overrides.map { FeatureFlagResolver.resolve(flag, environment, emptyMap(), it).enabled }
    override fun observeAll(): Flow<List<ResolvedFeatureFlag>> = overrides.map { FeatureFlagResolver.resolveAll(environment, overrides = it) }
    fun set(flag: FeatureFlag, enabled: Boolean) { overrides.value = overrides.value + (flag.key to enabled) }
}
