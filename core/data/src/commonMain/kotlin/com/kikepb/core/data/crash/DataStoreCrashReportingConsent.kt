package com.kikepb.core.data.crash

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.kikepb.core.domain.crash.CrashReporter
import com.kikepb.core.domain.crash.CrashReportingConsent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreCrashReportingConsent(
    private val dataStore: DataStore<Preferences>,
    private val crashReporter: CrashReporter
) : CrashReportingConsent {

    override val isAvailable: Boolean get() = crashReporter.isAvailable

    // Opt-in: no stored choice means no consent
    override fun observe(): Flow<Boolean> = dataStore.data.map { it[KEY] == true }

    override suspend fun set(enabled: Boolean) {
        dataStore.edit { it[KEY] = enabled }
        crashReporter.setCollectionEnabled(enabled)
    }

    override suspend fun applyStored() {
        crashReporter.setCollectionEnabled(observe().first())
    }

    private companion object {
        val KEY = booleanPreferencesKey("crash_reporting_consent")
    }
}

/** Platforms without a crash SDK. */
class NoOpCrashReporter : CrashReporter {
    override val isAvailable: Boolean = false
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}
