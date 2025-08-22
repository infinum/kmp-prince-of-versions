package com.infinum.princeofversions

import com.infinum.princeofversions.models.UpdateResult

/**
 * Represents the main interface for using the library.
 *
 * This library checks for application updates by fetching a configuration from a given source.
 *
 */
public interface PrinceOfVersions<T> {

    /**
     * Starts a check for an update.
     *
     * @param source The source from which to load the update configuration.
     *
     * @return An [UpdateResult] instance that contains the result of the update check.
     */
    public suspend fun checkForUpdates(
        source: Loader,
    ): UpdateResult<T>

    /**
     * Starts a check for an update.
     *
     * @param url The network url from which to load the update configuration
     * @param username Optional username for basic authentication.
     * @param password Optional password for basic authentication.
     * @param networkTimeoutSeconds Network timeout in seconds. Default is 60 seconds.
     *
     * @return An [UpdateResult] instance that contains the result of the update check.
     */
    public suspend fun checkForUpdates(
        url: String,
        username: String? = null,
        password: String? = null,
        networkTimeoutSeconds: Int = DEFAULT_NETWORK_TIMEOUT_SECONDS,
    ): UpdateResult<T>

    private companion object {
        private const val DEFAULT_NETWORK_TIMEOUT_SECONDS = 60
    }

}

internal expect class PrinceOfVersionsImpl<T> : PrinceOfVersions<T> {
    override suspend fun checkForUpdates(source: Loader): UpdateResult<T>
    override suspend fun checkForUpdates(
        url: String,
        username: String?,
        password: String?,
        networkTimeoutSeconds: Int
    ): UpdateResult<T>
}
