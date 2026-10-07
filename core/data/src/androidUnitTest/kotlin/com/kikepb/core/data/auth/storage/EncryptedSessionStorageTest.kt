package com.kikepb.core.data.auth.storage

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kikepb.core.domain.auth.model.AuthInfoModel
import com.kikepb.core.domain.auth.model.UserModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EncryptedSessionStorageTest {

    /** Reversible fake: the real one is AES-GCM in the Android Keystore, not available on the JVM. */
    private class FakeCipher : SessionCipher {
        override fun encrypt(plain: String) = plain.reversed()
        override fun decrypt(encoded: String) = encoded.reversed().takeIf { it.startsWith("{") }
    }

    private val key = stringPreferencesKey("KEY_AUTH_INFO")
    private val file = File.createTempFile("session", ".preferences_pb").apply { delete() }
    private val dataStore = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + SupervisorJob())) { file }
    private val storage = DataStoreSessionStorage(dataStore, FakeCipher())

    private val session = AuthInfoModel(
        accessToken = "access",
        refreshToken = "refresh",
        user = UserModel(id = "u-1", email = "a@squadfy.test", username = "ana", hasVerifiedEmail = true, profilePictureUrl = null)
    )

    @Test
    fun `AC-011-05 the session is stored encrypted and read back`() = runTest {
        storage.set(session)

        val raw = dataStore.data.first()[key]!!
        assertTrue(raw.startsWith("enc1:"))
        assertFalse(raw.contains("refresh"), "tokens must not be stored in plain text")
        assertEquals(session, storage.observeAuthInfo().first())
    }

    @Test
    fun `AC-011-05 a plain-text session from older versions is migrated`() = runTest {
        dataStore.edit {
            it[key] = """{"accessToken":"access","refreshToken":"refresh","user":{"id":"u-1","email":"a@squadfy.test","username":"ana","hasVerifiedEmail":true}}"""
        }

        assertEquals("refresh", storage.observeAuthInfo().first()?.refreshToken)
        assertTrue(dataStore.data.first()[key]!!.startsWith("enc1:"))
    }

    @Test
    fun `AC-011-05 an undecryptable session counts as logged out`() = runTest {
        dataStore.edit { it[key] = "enc1:garbage" }

        assertNull(storage.observeAuthInfo().first())
    }
}
