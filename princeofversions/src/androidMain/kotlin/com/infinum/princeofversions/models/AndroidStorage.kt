package com.infinum.princeofversions.models

internal class AndroidStorage : Storage<Int> {
    override suspend fun getLastSavedVersion(): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun saveVersion(version: Int) {
        TODO("Not yet implemented")
    }
}
