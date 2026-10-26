package com.kikepb.core.domain.analytics

import kotlinx.coroutines.flow.Flow

/**
 * Product analytics (spec 019 AC-019-18). Only with the user's consent, and never with personal data: no names, emails,
 * chat text or free text typed by the user, and no user id.
 */
interface AnalyticsTracker {
    /** False when this build has no analytics SDK, so the UI hides the option. */
    val isAvailable: Boolean
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
    override val isAvailable: Boolean = false
    override fun logEvent(event: AnalyticsEvent) = Unit
    override fun logScreenView(screenName: String) = Unit
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}

/**
 * Opt-in usage analytics (spec 019 AC-019-18), separate from the crash-report consent because it is a different
 * purpose. Nothing is collected until the user agrees in Profile; the choice is stored on the device.
 */
interface AnalyticsConsent {
    val isAvailable: Boolean
    fun observe(): Flow<Boolean>
    suspend fun set(enabled: Boolean)
    /** Pushes the stored choice to the analytics SDK. Called once at startup. */
    suspend fun applyStored()
}
