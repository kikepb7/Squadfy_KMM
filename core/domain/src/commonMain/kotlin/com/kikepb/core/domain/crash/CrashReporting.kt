package com.kikepb.core.domain.crash

import kotlinx.coroutines.flow.Flow

/**
 * Opt-in crash reports (spec 011 AC-011-13). Nothing is sent until the user agrees in Profile;
 * the choice is stored on the device and can be withdrawn at any time.
 */
interface CrashReportingConsent {
    /** False when this platform has no crash reporter (iOS is out of the MVP), so the UI hides the option. */
    val isAvailable: Boolean
    fun observe(): Flow<Boolean>
    suspend fun set(enabled: Boolean)
    /** Pushes the stored choice to the crash SDK. Called once at startup. */
    suspend fun applyStored()
}

/** Platform crash SDK switch. */
interface CrashReporter {
    val isAvailable: Boolean
    fun setCollectionEnabled(enabled: Boolean)
}
