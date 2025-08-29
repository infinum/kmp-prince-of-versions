package com.infinum.princeofversions

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import kotlin.io.encoding.Base64

/**
 * Represents a concrete loader that loads a resource from the network using a provided URL.
 *
 * @param url The URL representing the resource locator.
 * @param username Optional username for Basic authentication.
 * @param password Optional password for Basic authentication.
 * @param networkTimeoutSeconds The network timeout in seconds.
 */
internal class JvmDefaultLoader(
    private val url: String,
    private val username: String?,
    private val password: String?,
    networkTimeoutSeconds: Int,
) : Loader {

    private companion object {
        /**
         * Default request timeout in seconds.
         */
        private const val MILLISECONDS_IN_SECOND = 1000
    }

    /**
     * Custom network timeout in milliseconds.
     */
    private val networkTimeoutMilliseconds = networkTimeoutSeconds * MILLISECONDS_IN_SECOND

    @Throws(IOException::class)
    override suspend fun load(): String {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        try {
            // Apply Basic Authentication if credentials are provided
            if (username != null && password != null) {
                val credentials = "$username:$password"
                val basicAuth = "Basic ${
                    Base64.encode(credentials.encodeToByteArray())
                }"
                connection.setRequestProperty("Authorization", basicAuth)
            }

            connection.connectTimeout = networkTimeoutMilliseconds
            connection.readTimeout = networkTimeoutMilliseconds

            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
