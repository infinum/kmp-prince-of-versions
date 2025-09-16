package com.infinum.princeofversions

/**
 * Represents a local device storage object that can be used to save and retrieve the application version.
 */
public interface BaseStorage<T> {

    /**
     * Returns the last saved version of the application.
     */
    public suspend fun getLastSavedVersion(): T?

    /**
     * Saves the version to the devices local storage
     */
    public suspend fun saveVersion(version: T)
}
