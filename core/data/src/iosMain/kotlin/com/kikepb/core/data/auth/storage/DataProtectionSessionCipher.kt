package com.kikepb.core.data.auth.storage

/**
 * iOS keeps the session file under Data Protection (encrypted by the OS while the device is locked) and out of
 * iCloud/iTunes backups (createDataStore.ios). No extra app-level encryption (spec 011 decision).
 */
class DataProtectionSessionCipher : SessionCipher {
    override fun encrypt(plain: String): String = plain
    override fun decrypt(encoded: String): String = encoded
}
