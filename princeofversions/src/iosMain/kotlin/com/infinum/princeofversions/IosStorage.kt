package com.infinum.princeofversions

import platform.Foundation.NSUserDefaults

/**
 * Represents a local device storage object that can be used to save and retrieve the application version.
 */
public typealias Storage = BaseStorage<String>

internal class IosStorage : Storage {
    override suspend fun getLastSavedVersion(): String? {
        val value = NSUserDefaults.standardUserDefaults.objectForKey(KEY_LAST_NOTIFIED_VERSION)
        return value as? String
    }

    override suspend fun saveVersion(version: String) {
        NSUserDefaults.standardUserDefaults.setObject(version, forKey = KEY_LAST_NOTIFIED_VERSION)
    }

    private companion object {
        private const val KEY_LAST_NOTIFIED_VERSION = "last_notified_version"
    }
}
