package com.infinum.princeofversions

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
     * @return An [BaseUpdateResult] instance that contains the result of the update check.
     */
    public suspend fun checkForUpdates(
        source: Loader,
    ): BaseUpdateResult<T>

    public companion object {
        public val DEFAULT_NETWORK_TIMEOUT: Duration = 60.seconds
    }
}
