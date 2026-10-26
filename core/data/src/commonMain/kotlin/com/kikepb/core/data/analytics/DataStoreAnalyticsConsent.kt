package com.kikepb.core.data.analytics

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.kikepb.core.domain.analytics.AnalyticsConsent
import com.kikepb.core.domain.analytics.AnalyticsTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreAnalyticsConsent(
    private val dataStore: DataStore<Preferences>,
    private val tracker: AnalyticsTracker
) : AnalyticsConsent {

    override val isAvailable: Boolean get() = tracker.isAvailable

    // Opt-in: no stored choice means no consent
    override fun observe(): Flow<Boolean> = dataStore.data.map { it[KEY] == true }

    override suspend fun set(enabled: Boolean) {
        dataStore.edit { it[KEY] = enabled }
        tracker.setCollectionEnabled(enabled)
    }

    override suspend fun applyStored() {
        tracker.setCollectionEnabled(observe().first())
    }

    private companion object {
        val KEY = booleanPreferencesKey("analytics_consent")
    }
}
