package com.infinum.princeofversions

/**
 * Represents a local device storage object that can be used to save and retrieve the application version.
 */
public typealias Storage = BaseStorage<String>

@Suppress("NotImplementedDeclaration")
internal class IosStorage : Storage {
    override suspend fun getLastSavedVersion(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun saveVersion(version: String) {
        TODO("Not yet implemented")
    }
}
