package com.infinum.princeofversions.models

import java.util.prefs.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * An implementation of Storage that uses the java.util.prefs.Preferences API for persistence
 * in a JVM/Desktop environment.
 *
 * @param clazz A class reference used to create a unique preferences node for the application.
 */
internal class JvmStorage(
    private val clazz: Class<*>
) : Storage<String> {

    private companion object Companion {
        /**
         * Defines the key for storing the last notified version code.
         */
        const val LAST_NOTIFIED_VERSION_KEY = "PrinceOfVersions_LastNotifiedUpdate"
    }

    // Get the preferences node specific to the provided class's package.
    private val preferences: Preferences by lazy {
        Preferences.userNodeForPackage(clazz)
    }

    /**
     * Retrieves the last saved version from the preferences store.
     *
     * @return The last notified version string, or null if not set.
     */
    override suspend fun getLastSavedVersion(): String? = withContext(Dispatchers.IO) {
        // The 'get' method returns the default value (null) if the key is not found.
        preferences.get(LAST_NOTIFIED_VERSION_KEY, null)
    }

    /**
     * Saves the given version to the preferences store.
     *
     * @param version The version string to save.
     */
    override suspend fun saveVersion(version: String) = withContext(Dispatchers.IO) {
        preferences.put(LAST_NOTIFIED_VERSION_KEY, version)
    }
}
