package org.kikepb.squadfy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.kikepb.core.domain.analytics.AnalyticsTracker
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import org.koin.compose.koinInject

/**
 * Spec 019 AC-019-18: one `screen_view` per destination. The name comes from the type-safe route's class, never from
 * its arguments, so no id or typed text reaches Analytics. Nothing is sent without consent (the SDK is disabled).
 */
@Composable
fun TrackScreenViews(navController: NavHostController, tracker: AnalyticsTracker = koinInject()) {
    LaunchedEffect(navController, tracker) {
        navController.currentBackStackEntryFlow
            .mapNotNull { screenNameOf(it.destination.route) }
            .distinctUntilChanged()
            .collect { tracker.logScreenView(it) }
    }
}

/** `com.kikepb.club…ClubGraphRoutes.ClubDetailRoute/{clubId}?logoUploadFailed={…}` → `ClubDetail`. */
internal fun screenNameOf(route: String?): String? {
    val qualifiedName = route?.substringBefore('/')?.substringBefore('?')?.takeIf { it.isNotBlank() } ?: return null
    return qualifiedName.substringAfterLast('.').removeSuffix("Route").ifBlank { null }
}
