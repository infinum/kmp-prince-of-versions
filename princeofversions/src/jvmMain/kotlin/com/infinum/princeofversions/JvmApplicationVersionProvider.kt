package com.infinum.princeofversions

import java.io.IOException
import java.util.Properties

/**
 * Loads and provides the current application version. Assumes the version is stored in a properties file
 * with a path of `/version.properties` containing a key `application.version`.
 *
 * @throws IOException if the properties file does not exist
 * @throws IllegalStateException if the expected key is not present in the properties file
 */
internal class JvmApplicationVersionProvider(
    private val versionFilePath: String = "/version.properties",
    private val versionKey: String = "application.version"
) : ApplicationVersionProvider<String> {

    private val applicationVersion: String by lazy {
        val inputStream = javaClass.getResourceAsStream(versionFilePath)
            ?: error("Resource file not found: $versionFilePath. Make sure it's in your resources folder.")

        try {
            val properties = Properties()
            inputStream.use { properties.load(it) }
            properties.getProperty(versionKey)
                ?: error("Key '$versionFilePath' not found in $versionFilePath.")
        } catch (e: IOException) {
            // Catch specific I/O errors during the loading process.
            throw IOException("Could not read properties from $versionFilePath.", e)
        }
    }

    override fun getVersion(): String = applicationVersion
}
