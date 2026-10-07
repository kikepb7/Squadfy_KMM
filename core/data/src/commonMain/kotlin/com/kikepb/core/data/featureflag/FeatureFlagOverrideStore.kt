package com.kikepb.core.data.featureflag

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Persists QA overrides keyed by feature flag key. */
interface FeatureFlagOverrideStore {
    fun observe(): Flow<Map<String, Boolean>>
    suspend fun set(key: String, enabled: Boolean?)
    suspend fun clear()
}

class DataStoreFeatureFlagOverrideStore(
    private val dataStore: DataStore<Preferences>
) : FeatureFlagOverrideStore {

    override fun observe(): Flow<Map<String, Boolean>> = dataStore.data.map { preferences ->
        preferences.asMap()
            .filterKeys { it.name.startsWith(KEY_PREFIX) }
            .mapNotNull { (key, value) -> (value as? Boolean)?.let { key.name.removePrefix(KEY_PREFIX) to it } }
            .toMap()
    }

    override suspend fun set(key: String, enabled: Boolean?) {
        val preferenceKey = booleanPreferencesKey(name = KEY_PREFIX + key)
        dataStore.edit { preferences ->
            if (enabled == null) preferences.remove(preferenceKey) else preferences[preferenceKey] = enabled
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.asMap().keys
                .filter { it.name.startsWith(KEY_PREFIX) }
                .forEach { preferences.remove(it) }
        }
    }

    private companion object {
        const val KEY_PREFIX = "ff_"
    }
}
