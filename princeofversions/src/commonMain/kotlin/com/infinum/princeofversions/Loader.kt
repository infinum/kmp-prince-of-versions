package com.infinum.princeofversions

import com.infinum.princeofversions.PrinceOfVersionsBase.Companion.DEFAULT_NETWORK_TIMEOUT
import kotlin.time.Duration

/**
 * Represents a source from which the update configuration can be loaded.
 */
public fun interface Loader {

    /**
     * Loads the configuration content and returns it as a string.
     *
     * @return The configuration content as a [String].
     */
    public suspend fun load(): String
}

/**
 * Provides the platform specific default Loader implementation
 */
internal expect fun provideDefaultLoader(
    url: String,
    username: String? = null,
    password: String? = null,
    networkTimeout: Duration = DEFAULT_NETWORK_TIMEOUT,
): Loader
