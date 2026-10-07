package com.kikepb.core.data.notification

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.kikepb.core.domain.notification.NotificationPromptStore
import kotlinx.coroutines.flow.first

class DataStoreNotificationPromptStore(private val dataStore: DataStore<Preferences>) : NotificationPromptStore {

    override suspend fun wasAsked(): Boolean = dataStore.data.first()[KEY] == true

    override suspend fun markAsked() {
        dataStore.edit { it[KEY] = true }
    }

    private companion object {
        val KEY = booleanPreferencesKey("notification_permission_asked")
    }
}
