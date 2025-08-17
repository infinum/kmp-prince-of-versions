package com.infinum.princeofversions.models

import java.io.IOException
import java.util.Properties

/**
 * This class provides the application's version.
 *
 * It reads the version from a standard Java properties file located in the application's resources.
 * The file should be in a simple 'key=value' format. For example:
 * application.version=1.0.0
 *
 * @param filePath The path to the properties file within the application's resources.
 * Defaults to "/version.properties".
 * @param versionKey The key for the version property within the properties file.
 * Defaults to "application.version".
 */
internal class JvmApplicationConfiguration(
    private val filePath: String = "/version.properties",
    private val versionKey: String = "application.version"
) : ApplicationConfiguration<String> {

    /**
     * The application's version, loaded from the properties file.
     *
     * @throws IllegalStateException if the resource file cannot be found or
     * the version key is missing.
     * @throws IOException if an error occurs during file reading.
     */
    override val version: String by lazy {
        val inputStream = javaClass.getResourceAsStream(filePath)
            ?: error("Resource file not found: $filePath. Make sure it's in your resources folder.")

        try {
            val properties = Properties()
            inputStream.use { properties.load(it) }
            properties.getProperty(versionKey)
                ?: error("Key '$versionKey' not found in $filePath.")
        } catch (e: IOException) {
            // Catch specific I/O errors during the loading process.
            throw IOException("Could not read properties from $filePath.", e)
        }
    }
}
