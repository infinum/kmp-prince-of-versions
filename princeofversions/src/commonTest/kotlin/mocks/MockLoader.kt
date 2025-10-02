package mocks

import com.infinum.princeofversions.Loader

/**
 * Mock implementation of [Loader] for testing purposes.
 */
internal class MockLoader(
    private val result: String = DEFAULT_LOADER_RESULT,
) : Loader {

    override suspend fun load(): String = result

    private companion object {
        private const val DEFAULT_LOADER_RESULT = ""
    }
}