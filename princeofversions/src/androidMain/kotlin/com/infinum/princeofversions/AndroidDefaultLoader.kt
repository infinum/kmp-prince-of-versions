package com.infinum.princeofversions

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.io.encoding.Base64
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

    @Throws(IoException::class)
    override suspend fun load(): String {
        try {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = networkTimeoutMilliseconds
                readTimeout = networkTimeoutMilliseconds

                if (username != null && password != null) {
                    val auth = Base64.encode("$username:$password".encodeToByteArray())
                    setRequestProperty("Authorization", "Basic $auth")
                }
            }

            try {
                return connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        } catch (e: IOException) {
            throw IoException(e.message)
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
