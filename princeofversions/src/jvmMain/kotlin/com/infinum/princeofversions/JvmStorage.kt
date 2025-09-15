package com.infinum.princeofversions

internal class JvmStorage : Storage<String> {
    override suspend fun getLastSavedVersion(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun saveVersion(version: String) {
        TODO("Not yet implemented")
    }
}
