package com.infinum.princeofversions.models

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
