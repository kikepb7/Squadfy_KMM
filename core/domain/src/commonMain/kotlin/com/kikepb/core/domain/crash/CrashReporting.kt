package com.kikepb.core.domain.crash

import kotlinx.coroutines.flow.Flow

/**
 * Opt-in crash reports (spec 011 AC-011-13). Nothing is sent until the user agrees in Profile;
 * the choice is stored on the device and can be withdrawn at any time.
 */
interface CrashReportingConsent {
    /** False when this build has no crash reporter (the own-backend variant on iOS), so the UI hides the option. */
    val isAvailable: Boolean
    fun observe(): Flow<Boolean>
    suspend fun set(enabled: Boolean)
    /** Pushes the stored choice to the crash SDK. Called once at startup. */
    suspend fun applyStored()
}

/** Platform crash SDK. Breadcrumbs, non-fatals, user id and keys are no-ops where the SDK cannot take them. */
interface CrashReporter {
    val isAvailable: Boolean
    fun setCollectionEnabled(enabled: Boolean)
    fun log(message: String) = Unit
    fun recordException(throwable: Throwable, customKeys: Map<String, String> = emptyMap()) = Unit
    /** Pseudonymous id (the account uid) so a support request can be matched with its reports; null detaches it. */
    fun setUserId(userId: String?) = Unit
    fun setCustomKey(key: String, value: String) = Unit
}
