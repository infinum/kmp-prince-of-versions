package com.infinum.princeofversions

internal class IosDefaultLoader(
    url: String,
    username: String?,
    password: String?,
    networkTimeoutSeconds: Int
) : Loader {
    override suspend fun load(): String {
        TODO("Not yet implemented")
    }
}
