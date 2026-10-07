package com.kikepb.core.data.auth.storage

/**
 * Protects the stored session at rest (spec 011, AC-011-05).
 * Android: AES-GCM with a key that never leaves the Android Keystore.
 * iOS: the session file relies on iOS Data Protection and is excluded from backups (see createDataStore.ios).
 */
interface SessionCipher {
    fun encrypt(plain: String): String

    /** Null when the value cannot be decrypted (e.g. restored on another device): the session is dropped. */
    fun decrypt(encoded: String): String?
}
