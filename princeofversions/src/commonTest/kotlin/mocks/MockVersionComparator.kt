package mocks

import com.infinum.princeofversions.BaseVersionComparator

/**
 * Mock implementation of [BaseVersionComparator] for Integer version testing.
 */
internal class MockVersionComparator : BaseVersionComparator<Int> {
    override fun compare(firstVersion: Int, secondVersion: Int): Int {
        return firstVersion.compareTo(secondVersion)
    }
}