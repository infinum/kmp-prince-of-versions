package com.infinum.princeofversions

@Suppress("unused") // Remove once implementation is provided
internal class JvmDefaultLoader(
    url: String,
    username: String?,
    password: String?,
    networkTimeoutSeconds: Int
) : Loader {
    override suspend fun load(): String {
        TODO("Not yet implemented")
    }
}
