package org.kikepb.squadfy.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.core.domain.analytics.AnalyticsEvents
import com.kikepb.core.domain.analytics.AnalyticsTracker
import com.kikepb.core.domain.update.AppInfo
import com.kikepb.core.domain.update.AppUpdatePolicy
import com.kikepb.core.domain.update.AppVersionSource
import com.kikepb.core.domain.update.UpdatePromptStore
import com.kikepb.core.domain.update.UpdateStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What the update notice shows (spec 021 APP-RN-P8); [status] UP_TO_DATE hides it. */
data class AppUpdateState(
    val status: UpdateStatus = UpdateStatus.UP_TO_DATE,
    val latestVersion: String = "",
    val storeUrl: String = ""
)

sealed interface AppUpdateAction {
    data object OnUpdateClick : AppUpdateAction
    data object OnLaterClick : AppUpdateAction
}

/**
 * AC-021-08: below `min_version` the app asks to update and does not let the user go on; below `latest_version` it
 * offers the update once, until a newer one is published. The values come from Remote Config per platform and environment.
 */
class AppUpdateViewModel(
    appInfo: AppInfo,
    versionSource: AppVersionSource,
    private val promptStore: UpdatePromptStore,
    private val analytics: AnalyticsTracker
) : ViewModel() {

    val state: StateFlow<AppUpdateState> = combine(versionSource.observeVersions(), promptStore.observeDismissedVersion()) { config, dismissed ->
        AppUpdateState(
            status = AppUpdatePolicy.status(installed = appInfo.versionName, config = config, dismissedVersion = dismissed),
            latestVersion = config.latestVersion.ifBlank { config.minVersion },
            storeUrl = config.storeUrl
        )
    }
        .distinctUntilChanged()
        .onEach { if (it.status != UpdateStatus.UP_TO_DATE) analytics.logEvent(AnalyticsEvents.updatePromptShown(required = it.status == UpdateStatus.REQUIRED)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), AppUpdateState())

    fun onAction(action: AppUpdateAction) {
        when (action) {
            // Opening the store is the screen's job (LocalUriHandler); a required update stays on screen meanwhile
            AppUpdateAction.OnUpdateClick -> analytics.logEvent(AnalyticsEvents.updatePromptAccepted())
            AppUpdateAction.OnLaterClick -> {
                val current = state.value
                if (current.status == UpdateStatus.OPTIONAL) viewModelScope.launch { promptStore.dismiss(current.latestVersion.trim()) }
            }
        }
    }
}
