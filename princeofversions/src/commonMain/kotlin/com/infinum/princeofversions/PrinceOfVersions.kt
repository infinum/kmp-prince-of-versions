package com.infinum.princeofversions

import com.infinum.princeofversions.models.UpdateResult

/**
 * Represents the main entry point for using the library.
 *
 * This library checks for application updates by fetching a configuration from a given source.
 *
 */
public fun interface PrinceOfVersions<T> {

    /**
     * Starts a check for an update.
     *
     * @param source The source from which to load the update configuration (e.g., network URL).
     *
     * @return An [UpdateResult] instance that contains the result of the update check.
     */
    public suspend fun checkForUpdates(
        source: Loader,
    ): UpdateResult<T>

}

internal expect class PrinceOfVersionsImpl<T> : PrinceOfVersions<T> {
    override suspend fun checkForUpdates(source: Loader): UpdateResult<T>
}
