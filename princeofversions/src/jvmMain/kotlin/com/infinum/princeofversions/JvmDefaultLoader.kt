package com.infinum.princeofversions

import kotlin.time.Duration

@Suppress("unused") // Remove once implementation is provided
internal class JvmDefaultLoader(
    url: String,
    username: String?,
    password: String?,
    networkTimeout: Duration,
) : Loader {
    override suspend fun load(): String {
        TODO("Not yet implemented")
    }
}

internal actual fun provideDefaultLoader(
    url: String,
    username: String?,
    password: String?,
    networkTimeout: Duration,
): Loader = JvmDefaultLoader(
    url = url,
    username = username,
    password = password,
    networkTimeout = networkTimeout,
)
