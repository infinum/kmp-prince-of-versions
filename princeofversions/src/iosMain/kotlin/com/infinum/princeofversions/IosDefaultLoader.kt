package com.infinum.princeofversions

import kotlin.time.Duration

@Suppress("unused") // Remove once implementation is provided
internal class IosDefaultLoader(
    url: String,
    username: String?,
    password: String?,
    networkTimeout: Duration,
) : Loader {
    override suspend fun load(): String {
        TODO("Not yet implemented")
    }
}
