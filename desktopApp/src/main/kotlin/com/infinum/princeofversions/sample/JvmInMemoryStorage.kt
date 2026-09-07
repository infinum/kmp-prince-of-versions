package com.infinum.princeofversions.sample

import com.infinum.princeofversions.Storage
import kotlinx.coroutines.delay

/**
 * A JVM-specific in-memory storage implementation that adapts the common logic
 * to work with the <String> generic type used by the desktop components.
 * The value is lost when the app is closed.
 */
class JvmInMemoryStorage : Storage {
    private var lastSavedVersion: String? = null

    override suspend fun getLastSavedVersion(): String? {
        // Add a small delay to simulate real storage access
        delay(100)
        return lastSavedVersion
    }

    override suspend fun saveVersion(version: String) {
        // Add a small delay to simulate real storage access
        delay(100)
        lastSavedVersion = version
    }
}