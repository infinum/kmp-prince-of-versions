package com.infinum.princeofversions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class JvmDefaultVersionComparatorTest {

    private val comparator = JvmDefaultVersionComparator()

    @Test
    fun `compare should return zero when versions are equal`() {
        assertEquals(0, comparator.compare("1.0.0", "1.0.0"))
        assertEquals(0, comparator.compare("2.5.10", "2.5.10"))
        assertEquals(0, comparator.compare("0.0.1", "0.0.1"))
        assertEquals(0, comparator.compare("10", "10"))
    }

    @Test
    fun `compare should return positive when first version is greater`() {
        assertTrue(comparator.compare("2.0.0", "1.0.0") > 0)
        assertTrue(comparator.compare("1.1.0", "1.0.0") > 0)
        assertTrue(comparator.compare("1.0.1", "1.0.0") > 0)
        assertTrue(comparator.compare("1.10.0", "1.2.0") > 0)
        assertTrue(comparator.compare("10.0.0", "2.0.0") > 0)
    }

    @Test
    fun `compare should return negative when first version is smaller`() {
        assertTrue(comparator.compare("1.0.0", "2.0.0") < 0)
        assertTrue(comparator.compare("1.0.0", "1.1.0") < 0)
        assertTrue(comparator.compare("1.0.0", "1.0.1") < 0)
        assertTrue(comparator.compare("1.2.0", "1.10.0") < 0)
        assertTrue(comparator.compare("2.0.0", "10.0.0") < 0)
    }

    @Test
    fun `compare should handle versions with different number of parts`() {
        // Missing parts are treated as 0
        assertEquals(0, comparator.compare("1.0", "1.0.0"))
        assertEquals(0, comparator.compare("1.0.0", "1.0"))
        assertEquals(0, comparator.compare("2", "2.0.0"))
        assertTrue(comparator.compare("1.0.1", "1.0") > 0)
        assertTrue(comparator.compare("1.0", "1.0.1") < 0)
        assertTrue(comparator.compare("2.1", "2.0.5") > 0)
    }

    @Test
    fun `compare should handle single digit versions`() {
        assertEquals(0, comparator.compare("1", "1"))
        assertTrue(comparator.compare("2", "1") > 0)
        assertTrue(comparator.compare("1", "2") < 0)
        assertTrue(comparator.compare("10", "9") > 0)
    }

    @Test
    fun `compare should handle large version numbers`() {
        assertTrue(comparator.compare("1.999.0", "1.1000.0") < 0)
        assertTrue(comparator.compare("999.0.0", "1000.0.0") < 0)
        assertEquals(0, comparator.compare("12345.67890.99999", "12345.67890.99999"))
    }

    @Test
    fun `compare should handle versions with leading zeros correctly`() {
        assertEquals(0, comparator.compare("1.01.1", "1.1.1"))
        assertEquals(0, comparator.compare("01.0.0", "1.0.0"))
        assertTrue(comparator.compare("1.02.0", "1.1.0") > 0)
    }

    @Test
    fun `compare should handle versions with whitespace`() {
        assertEquals(0, comparator.compare(" 1.0.0 ", "1.0.0"))
        assertEquals(0, comparator.compare("1.0.0", " 1.0.0 "))
        assertTrue(comparator.compare(" 2.0.0 ", " 1.0.0 ") > 0)
    }

    @Test
    fun `compare should throw exception for blank version strings`() {
        assertFailsWith<IllegalArgumentException> {
            comparator.compare("", "1.0.0")
        }
        assertFailsWith<IllegalArgumentException> {
            comparator.compare("1.0.0", "")
        }
        assertFailsWith<IllegalArgumentException> {
            comparator.compare("   ", "1.0.0")
        }
    }

    @Test
    fun `compare should throw exception for versions with empty parts`() {
        assertFailsWith<IllegalArgumentException> {
            comparator.compare("1..0", "1.0.0")
        }
        assertFailsWith<IllegalArgumentException> {
            comparator.compare("1.0.", "1.0.0")
        }
        assertFailsWith<IllegalArgumentException> {
            comparator.compare(".1.0", "1.0.0")
        }
        assertFailsWith<IllegalArgumentException> {
            comparator.compare("1.0.0", "1..0")
        }
    }

    @Test
    fun `compare should throw exception for non-numeric version parts`() {
        assertFailsWith<IllegalArgumentException> {
            comparator.compare("1.a.0", "1.0.0")
        }
        assertFailsWith<IllegalArgumentException> {
            comparator.compare("1.0.0", "1.b.0")
        }
        assertFailsWith<IllegalArgumentException> {
            comparator.compare("v1.0.0", "1.0.0")
        }
        assertFailsWith<IllegalArgumentException> {
            comparator.compare("1.0.0-beta", "1.0.0")
        }
    }

    @Test
    fun `compare should handle complex real-world version scenarios`() {
        // Typical app version progression
        assertTrue(comparator.compare("1.0.1", "1.0.0") > 0)
        assertTrue(comparator.compare("1.1.0", "1.0.9") > 0)
        assertTrue(comparator.compare("2.0.0", "1.9.9") > 0)

        // Common version numbering patterns
        assertTrue(comparator.compare("1.10.0", "1.9.0") > 0)
        assertTrue(comparator.compare("1.0.10", "1.0.9") > 0)
        assertTrue(comparator.compare("10.0.0", "9.99.99") > 0)

        // Edge cases with missing components
        assertTrue(comparator.compare("1.1", "1.0.9") > 0)
        assertTrue(comparator.compare("2", "1.99.99") > 0)
    }

    @Test
    fun `compare should be symmetric`() {
        val version1 = "1.2.3"
        val version2 = "1.2.4"

        val result1 = comparator.compare(version1, version2)
        val result2 = comparator.compare(version2, version1)

        // If version1 < version2, then version2 > version1
        assertTrue(result1 < 0)
        assertTrue(result2 > 0)
        assertEquals(result1, -result2)
    }

    @Test
    fun `compare should be transitive`() {
        val version1 = "1.0.0"
        val version2 = "1.1.0"
        val version3 = "1.2.0"

        assertTrue(comparator.compare(version1, version2) < 0)
        assertTrue(comparator.compare(version2, version3) < 0)
        assertTrue(comparator.compare(version1, version3) < 0)
    }
}
