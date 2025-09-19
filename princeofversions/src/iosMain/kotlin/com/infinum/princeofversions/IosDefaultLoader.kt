package com.infinum.princeofversions

import kotlin.time.Duration

@Suppress("unused") // Remove once implementation is provided
internal class IosDefaultLoader(
    url: String,
    username: String?,
    password: String?,
    networkTimeout: Duration,
) : Loader {
    @Suppress("NotImplementedDeclaration")
    override suspend fun load(): String {
        TODO("Not yet implemented")
    }
}

internal actual fun provideDefaultLoader(
    url: String,
    username: String?,
    password: String?,
    networkTimeout: Duration,
): Loader = IosDefaultLoader(
    url = url,
    username = username,
    password = password,
    networkTimeout = networkTimeout,
)
