package com.kikepb.core.data.update

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kikepb.core.domain.update.UpdatePromptStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The optional update the user dismissed (spec 021 APP-RN-P8), kept on the device. */
class DataStoreUpdatePromptStore(private val dataStore: DataStore<Preferences>) : UpdatePromptStore {

    override fun observeDismissedVersion(): Flow<String?> = dataStore.data.map { it[KEY] }

    override suspend fun dismiss(version: String) {
        dataStore.edit { it[KEY] = version }
    }

    private companion object {
        val KEY = stringPreferencesKey("dismissed_update_version")
    }
}
