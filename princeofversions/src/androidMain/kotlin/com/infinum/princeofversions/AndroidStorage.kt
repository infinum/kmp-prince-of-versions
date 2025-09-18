package com.infinum.princeofversions

/**
 * Represents a local device storage object that can be used to save and retrieve the application version.
 */
public typealias Storage = BaseStorage<Int>

@Suppress("NotImplementedDeclaration")
internal class AndroidStorage : Storage {
    override suspend fun getLastSavedVersion(): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun saveVersion(version: Int) {
        TODO("Not yet implemented")
    }
}
