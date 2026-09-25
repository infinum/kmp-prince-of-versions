package com.infinum.princeofversions

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import kotlin.io.encoding.Base64
import kotlin.time.Duration

/**
 * Represents a concrete loader that loads a resource from the network using a provided URL.
 *
 * @param url The URL representing the resource locator.
 * @param username Optional username for Basic authentication.
 * @param password Optional password for Basic authentication.
 * @param networkTimeout The network timeout duration.
 * @param headers Additional HTTP headers sent with the request.
 */

internal class JvmDefaultLoader(
    private val url: String,
    private val username: String?,
    private val password: String?,
    networkTimeout: Duration,
    private val headers: Map<String, String> = emptyMap(),
) : Loader {

    /**
     * Custom network timeout in milliseconds.
     */
    private val networkTimeoutMilliseconds = networkTimeout.inWholeMilliseconds.toInt()

    @Throws(IOException::class)
    override suspend fun load(): String {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        try {
            headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }

            // Apply Basic Authentication if credentials are provided. Applied after custom headers,
            // so credentials take precedence over an `Authorization` entry.
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

internal actual fun provideDefaultLoader(
    url: String,
    username: String?,
    password: String?,
    networkTimeout: Duration,
    headers: Map<String, String>,
): Loader = JvmDefaultLoader(
    url = url,
    username = username,
    password = password,
    networkTimeout = networkTimeout,
    headers = headers,
)
