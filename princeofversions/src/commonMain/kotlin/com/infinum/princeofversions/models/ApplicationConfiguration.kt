package com.infinum.princeofversions.models

/**
 * Provides application parameters such as the application's version.
 */
internal interface ApplicationConfiguration<T> {
    /**
     * The application's version code.
     */
    val version: T
}
