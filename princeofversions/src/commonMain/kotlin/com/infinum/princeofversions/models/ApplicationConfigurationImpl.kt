package com.infinum.princeofversions.models

/**
 * This class provides the application's version.
 */
internal expect class ApplicationConfigurationImpl : ApplicationConfiguration {

    /**
     * The application's version code.
     */
    override val version: String
}
