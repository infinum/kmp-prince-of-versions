package com.infinum.princeofversions

/**
 * Represents a source from which the update configuration can be loaded.
 */
public interface ConfigSource {

    /**
     * Loads the configuration content and returns it as a string.
     *
     * This is a suspend function, meaning it's designed for asynchronous work.
     * It can perform long-running operations (like network requests) without
     * blocking the thread that it was called from.
     *
     * On platforms that support coroutines (like Android), this should be called
     * from a coroutine scope. On platforms that don't (like iOS), the KMP compiler
     * will expose this as a function with a completion handler.
     *
     * @return The configuration content as a [String].
     */
    public suspend fun load(): String
}
