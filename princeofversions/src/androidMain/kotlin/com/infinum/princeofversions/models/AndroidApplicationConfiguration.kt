package com.infinum.princeofversions.models

import com.infinum.princeofversions.ApplicationVersionProvider

/**
 * Provides application parameters such as the application's version.
 *
 * @param versionProvider A provider to retrieve the application's version code.
 */
internal class AndroidApplicationConfiguration(
    versionProvider: ApplicationVersionProvider<Int>,
) : ApplicationConfiguration<Int> {

    /**
     * The application's version code, retrieved from the package manager.
     * This implementation is backward-compatible and safely casts the version code to an Int.
     * @throws IllegalStateException if the application's package name cannot be found,
     * or if the version code is too large to be represented as an Int.
     */
    override val version: Int = versionProvider.getVersion()
}
