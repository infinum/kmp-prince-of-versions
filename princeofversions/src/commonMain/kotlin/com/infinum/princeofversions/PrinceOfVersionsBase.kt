package com.infinum.princeofversions

import com.infinum.princeofversions.models.UpdateResult
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val DEFAULT_NETWORK_TIMEOUT = 60.seconds

/**
 * Represents the base generic interface for using the library.
 *
 * This library checks for application updates by fetching a configuration from a given source.
 *
 */
public interface PrinceOfVersionsBase<T> {

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

}

/**
 * Starts a check for an update.
 *
 * @param url The network url from which to load the update configuration
 * @param username Optional username for basic authentication.
 * @param password Optional password for basic authentication.
 * @param networkTimeout Network timeout. Default is 60 seconds.
 *
 * @return An [UpdateResult] instance that contains the result of the update check.
 */
public expect suspend fun <T> PrinceOfVersionsBase<T>.checkForUpdates(
    url: String,
    username: String? = null,
    password: String? = null,
    networkTimeout: Duration = DEFAULT_NETWORK_TIMEOUT,
): UpdateResult<T>

internal expect class PrinceOfVersionsBaseImpl<T> : PrinceOfVersionsBase<T> {
    override suspend fun checkForUpdates(source: Loader): UpdateResult<T>
}
