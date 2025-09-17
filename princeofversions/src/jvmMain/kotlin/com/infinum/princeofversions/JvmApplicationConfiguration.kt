package com.infinum.princeofversions

/**
 * Provides application parameters such as the application's version.
 *
 * @param versionProvider A provider to fetch the application's version.
 */
internal class JvmApplicationConfiguration(
    versionProvider: ApplicationVersionProvider,
) : ApplicationConfiguration<String> {

    /**
     * The application's version
     */
    override val version: String = versionProvider.getVersion()
}
