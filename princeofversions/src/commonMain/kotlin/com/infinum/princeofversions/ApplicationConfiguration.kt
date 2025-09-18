package com.infinum.princeofversions

/**
 * Provides application parameters such as the application's version.
 */
internal interface ApplicationConfiguration<T> {
    /**
     * The application's version code.
     */
    val version: T
}
