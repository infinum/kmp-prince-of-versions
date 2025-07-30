package com.infinum.princeofversions.models

/**
 * This class provides the application's version and the device's SDK version code.
 *
 * @param context The application context used to retrieve package information.
 */
internal expect class ApplicationConfigurationImpl : ApplicationConfiguration {

    /**
     * The application's version code, retrieved from the package manager.
     * This implementation is backward-compatible and safely casts the version code to an Int.
     * @throws IllegalStateException if the application's package name cannot be found,
     * or if the version code is too large to be represented as an Int.
     */
    override val version: String

    /**
     * The SDK version of the Android OS on which the app is running.
     */
    override val sdkVersionCode: String
}
