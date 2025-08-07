package com.infinum.princeofversions.models

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private const val FILE_NAME = "com.infinum.princeofversions.PREFERENCES"

/**
 * A top-level property delegate to create a single instance of DataStore for the application.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = FILE_NAME)

/**
 * An implementation of Storage that uses Jetpack DataStore for persistence.
 */
internal class AndroidStorage(private val context: Context) : Storage<Int> {

    private companion object {
        /**
         * Defines the key for storing the last notified version code in DataStore.
         */
        val LAST_NOTIFIED_VERSION_KEY = intPreferencesKey("PrinceOfVersions_LastNotifiedUpdate")
    }

    /**
     * Retrieves the last saved version from DataStore asynchronously.
     *
     * @return The last notified version code, or null if not set.
     */
    override suspend fun getLastSavedVersion(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[LAST_NOTIFIED_VERSION_KEY]
    }

    /**
     * Saves the given version to DataStore asynchronously.
     *
     * @param version The version code to save.
     */
    override suspend fun saveVersion(version: Int) {
        context.dataStore.edit { settings ->
            settings[LAST_NOTIFIED_VERSION_KEY] = version
        }
    }
}
