package com.infinum.princeofversions

import platform.Foundation.NSUserDefaults

internal class IosAppStoreStorage : Storage {

    override suspend fun getLastSavedVersion(): String? {
        val value = NSUserDefaults.standardUserDefaults.objectForKey(KEY)
        return value as? String
    }

    override suspend fun saveVersion(version: String) {
        NSUserDefaults.standardUserDefaults.setObject(version, forKey = KEY)
    }

    private companion object {
        private const val KEY = "last_notified_appstore_version"
    }
}
