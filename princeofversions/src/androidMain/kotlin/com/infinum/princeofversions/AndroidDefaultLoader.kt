package com.infinum.princeofversions

import android.util.Base64
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.time.Duration

/**
 * Represents a concrete loader that loads a resource from the network using a provided URL.
 *
 * @param url The URL representing the resource locator.
 * @param username Optional username for Basic authentication.
 * @param password Optional password for Basic authentication.
 * @param networkTimeout Custom network timeout duration.
 */

internal class AndroidDefaultLoader(
    private val url: String,
    private val username: String?,
    private val password: String?,
    networkTimeout: Duration,
) : Loader {

    /**
     * Custom network timeout in milliseconds.
     */
    private val networkTimeoutMilliseconds = networkTimeout.inWholeMilliseconds.toInt()

    @Throws(IOException::class)
    override suspend fun load(): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            // Apply Basic Authentication if credentials are provided
            if (username != null && password != null) {
                val credentials = "$username:$password"
                val basicAuth = "Basic ${
                    Base64.encodeToString(credentials.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
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
): Loader = AndroidDefaultLoader(
    url = url,
    username = username,
    password = password,
    networkTimeout = networkTimeout,
)
