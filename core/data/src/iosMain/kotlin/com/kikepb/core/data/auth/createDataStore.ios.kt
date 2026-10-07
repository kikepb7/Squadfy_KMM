package com.kikepb.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.kikepb.core.data.auth.storage.DATA_STORE_FILE_NAME
import com.kikepb.core.data.auth.storage.createDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileProtectionCompleteUntilFirstUserAuthentication
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUserDomainMask

/**
 * Session and preferences live in Application Support (not the user-visible Documents folder), protected by iOS
 * Data Protection and excluded from iCloud/iTunes backups (spec 011, AC-011-05).
 */
@OptIn(ExperimentalForeignApi::class)
fun createDataStore(): DataStore<Preferences> {
    return createDataStore {
        val fileManager = NSFileManager.defaultManager
        val base = requireNotNull(
            fileManager.URLForDirectory(
                directory = NSApplicationSupportDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null
            )
        )
        val directory = requireNotNull(base.URLByAppendingPathComponent("squadfy", isDirectory = true))
        fileManager.createDirectoryAtURL(
            url = directory,
            withIntermediateDirectories = true,
            attributes = mapOf<Any?, Any?>(NSFileProtectionKey to NSFileProtectionCompleteUntilFirstUserAuthentication),
            error = null
        )
        directory.setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
        requireNotNull(directory.path) + "/$DATA_STORE_FILE_NAME"
    }
}
