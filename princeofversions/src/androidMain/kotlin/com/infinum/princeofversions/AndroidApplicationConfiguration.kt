package com.infinum.princeofversions

/**
 * Provides application parameters such as the application's version.
 *
 * @param versionProvider A provider to retrieve the application's version code.
 */
internal class AndroidApplicationConfiguration(
    versionProvider: ApplicationVersionProvider,
) : ApplicationConfiguration<Long> {

    /**
     * The application's version code, retrieved from the version provider.
     */
    override val version: Long = versionProvider.getVersion()
}
