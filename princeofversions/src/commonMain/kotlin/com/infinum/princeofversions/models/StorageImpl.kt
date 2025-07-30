package com.infinum.princeofversions.models

/**
 * Represents a local device storage object that can be used to save and retrieve the application version.
 */
internal expect class StorageImpl: Storage {

    /**
     * Returns the last saved version of the application.
     */
    override fun getLastSavedVersion(): String?

    /**
     * Saves the version to the devices local storage
     */
    override fun saveVersion(version: String)
}
