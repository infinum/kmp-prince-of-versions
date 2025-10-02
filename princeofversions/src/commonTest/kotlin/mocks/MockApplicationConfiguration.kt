package mocks

import com.infinum.princeofversions.ApplicationConfiguration

/**
 * Mock implementation of [ApplicationConfiguration] for testing purposes.
 */
internal class MockApplicationConfiguration<T>(
    override val version: T,
) : ApplicationConfiguration<T>