package com.infinum.princeofversions.models

/**
 * Represents a local device storage object that can be used to save and retrieve the application version.
 */
internal interface Storage {

    /**
     * Returns the last saved version of the application.
     */
    fun getLastSavedVersion(): String?

    /**
     * Saves the version to the devices local storage
     */
    fun saveVersion(version: String)
}
