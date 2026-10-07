package com.kikepb.core.data.auth.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kikepb.core.data.auth.dto.AuthInfoSerializableDTO
import com.kikepb.core.data.mappers.toDomain
import com.kikepb.core.data.mappers.toDto
import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.repository.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Session (tokens + user) in DataStore, encrypted with [SessionCipher] (spec 011, AC-011-05).
 * Sessions saved in plain text by older versions are read once and saved again encrypted.
 */
class DataStoreSessionStorage(
    private val dataStore: DataStore<Preferences>,
    private val cipher: SessionCipher
) : SessionStorage {

    private val authInfoKey = stringPreferencesKey("KEY_AUTH_INFO")
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun observeAuthInfo(): Flow<AuthInfoModel?> =
        dataStore.data.map { preferences ->
            val stored = preferences[authInfoKey] ?: return@map null
            if (stored.startsWith(ENCRYPTED_PREFIX)) {
                cipher.decrypt(stored.removePrefix(ENCRYPTED_PREFIX))?.let(::decode)
            } else {
                // Legacy plain-text session: migrate it in place
                decode(stored)?.also { set(it) }
            }
        }

    override suspend fun set(info: AuthInfoModel?) {
        if (info == null) {
            dataStore.edit { it.remove(key = authInfoKey) }
            return
        }
        val serialized = json.encodeToString(value = info.toDto())
        dataStore.edit { prefs ->
            prefs[authInfoKey] = ENCRYPTED_PREFIX + cipher.encrypt(serialized)
        }
    }

    private fun decode(serialized: String): AuthInfoModel? =
        runCatching { json.decodeFromString<AuthInfoSerializableDTO>(string = serialized).toDomain() }.getOrNull()

    private companion object {
        const val ENCRYPTED_PREFIX = "enc1:"
    }
}
