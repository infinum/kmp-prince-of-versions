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
     * The application's version code, retrieved from the version provider.
     */
    override val version: Int = versionProvider.getVersion()
}
