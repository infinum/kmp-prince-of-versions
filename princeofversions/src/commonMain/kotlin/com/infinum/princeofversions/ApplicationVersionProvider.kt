package com.infinum.princeofversions

/**
 * Provides the current version of the application.
 */
public fun interface ApplicationVersionProvider<T> {
    public fun getVersion(): T
}
