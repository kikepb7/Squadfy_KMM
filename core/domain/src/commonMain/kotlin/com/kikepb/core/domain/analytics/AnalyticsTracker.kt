package com.kikepb.core.domain.analytics

/**
 * Product analytics (spec 019 AC-019-18). Only with the user's consent, and never with personal data: no names, emails,
 * chat text or free text typed by the user, and no user id.
 */
interface AnalyticsTracker {
    fun logEvent(event: AnalyticsEvent)
    fun logScreenView(screenName: String)
    fun setCollectionEnabled(enabled: Boolean)
}

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, String> = emptyMap()
)

/** Builds without an analytics SDK (the frozen own-backend variant). */
object NoOpAnalyticsTracker : AnalyticsTracker {
    override fun logEvent(event: AnalyticsEvent) = Unit
    override fun logScreenView(screenName: String) = Unit
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}
