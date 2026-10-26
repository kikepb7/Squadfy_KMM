package com.kikepb.core.firebase.analytics

import com.kikepb.core.domain.analytics.AnalyticsEvent
import com.kikepb.core.domain.analytics.AnalyticsTracker
import dev.gitlive.firebase.analytics.FirebaseAnalytics

/** Google Analytics for Firebase, off until the user agrees (spec 019 AC-019-18). No user id is ever set. */
class FirebaseAnalyticsTracker(
    private val analytics: Lazy<FirebaseAnalytics>
) : AnalyticsTracker {

    override fun logEvent(event: AnalyticsEvent) {
        analytics.value.logEvent(name = event.name, parameters = event.params.takeIf { it.isNotEmpty() })
    }

    override fun logScreenView(screenName: String) {
        analytics.value.logEvent(
            name = "screen_view",
            parameters = mapOf("screen_name" to screenName, "screen_class" to screenName)
        )
    }

    override fun setCollectionEnabled(enabled: Boolean) = analytics.value.setAnalyticsCollectionEnabled(enabled)
}
