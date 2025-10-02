package mocks

import com.infinum.princeofversions.BaseStorage

/**
 * Mock implementation of [BaseStorage] for testing purposes.
 */
internal class MockStorage<T> : BaseStorage<T> {
    private var savedVersion: T? = null

    override suspend fun getLastSavedVersion(): T? = savedVersion

    override suspend fun saveVersion(version: T) {
        savedVersion = version
    }

    fun setSavedVersion(version: T?) {
        savedVersion = version
    }

    fun clearSavedVersion() {
        savedVersion = null
    }
}