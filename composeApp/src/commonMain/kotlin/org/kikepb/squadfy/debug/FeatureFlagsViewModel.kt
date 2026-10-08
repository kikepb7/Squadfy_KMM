package org.kikepb.squadfy.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.core.domain.featureflag.AppEnvironment
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FeatureFlagOverrides
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.core.domain.featureflag.ResolvedFeatureFlag
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FeatureFlagsViewModel(
    private val featureFlags: FeatureFlags,
    private val overrides: FeatureFlagOverrides
) : ViewModel() {

    val state = featureFlags.observeAll()
        .map { flags -> FeatureFlagsState(environment = featureFlags.environment, flags = flags) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
            initialValue = FeatureFlagsState(environment = featureFlags.environment)
        )

    fun onAction(action: FeatureFlagsAction) {
        when (action) {
            is FeatureFlagsAction.OnToggle -> viewModelScope.launch {
                // Back to the environment default clears the override instead of pinning the same value
                val enabled = action.enabled.takeIf { it != action.flag.defaultFor(featureFlags.environment, featureFlags.platform) }
                overrides.setOverride(flag = action.flag, enabled = enabled)
            }
            FeatureFlagsAction.OnReset -> viewModelScope.launch { overrides.clearOverrides() }
            FeatureFlagsAction.OnBackClick -> Unit
        }
    }
}

data class FeatureFlagsState(
    val environment: AppEnvironment,
    val flags: List<ResolvedFeatureFlag> = emptyList()
)

sealed interface FeatureFlagsAction {
    data class OnToggle(val flag: FeatureFlag, val enabled: Boolean) : FeatureFlagsAction
    data object OnReset : FeatureFlagsAction
    data object OnBackClick : FeatureFlagsAction
}
