package com.infinum.princeofversions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class JvmVersionRequirementCheckerTest {

    private val checker = JvmVersionRequirementChecker()

    @Test
    fun `checkRequirements should return true when current JVM version meets requirement`() {
        // Assuming we're running on JVM 17 or higher for most test environments
        assertTrue(checker.checkRequirements("8"))
        assertTrue(checker.checkRequirements("11"))
    }

    @Test
    fun `checkRequirements should return false when current JVM version is below requirement`() {
        // Test with a very high version requirement that current JVM won't meet
        assertFalse(checker.checkRequirements("99"))
    }

    @Test
    fun `checkRequirements should return true when requirement equals current version`() {
        // Get the current Java version to test exact match
        val currentVersion = getCurrentJavaVersionForTest()
        assertTrue(checker.checkRequirements(currentVersion.toString()))
    }

    @Test
    fun `checkRequirements should throw exception when value is null`() {
        assertFailsWith<IllegalArgumentException> {
            checker.checkRequirements(null)
        }
    }

    @Test
    fun `checkRequirements should throw exception when value is empty string`() {
        assertFailsWith<IllegalArgumentException> {
            checker.checkRequirements("")
        }
    }

    @Test
    fun `checkRequirements should throw exception when value is blank string`() {
        assertFailsWith<IllegalArgumentException> {
            checker.checkRequirements("   ")
        }
    }

    @Test
    fun `checkRequirements should throw exception when value is not a valid integer`() {
        assertFailsWith<IllegalArgumentException> {
            checker.checkRequirements("abc")
        }
        assertFailsWith<IllegalArgumentException> {
            checker.checkRequirements("11.5")
        }
        assertFailsWith<IllegalArgumentException> {
            checker.checkRequirements("1.8")
        }
    }

    @Test
    fun `checkRequirements should throw exception when value contains non-numeric characters`() {
        assertFailsWith<IllegalArgumentException> {
            checker.checkRequirements("11a")
        }
        assertFailsWith<IllegalArgumentException> {
            checker.checkRequirements("v11")
        }
        assertFailsWith<IllegalArgumentException> {
            checker.checkRequirements("11+")
        }
    }

    @Test
    fun `checkRequirements should handle zero version requirement`() {
        // All JVM versions should be >= 0
        assertTrue(checker.checkRequirements("0"))
    }

    @Test
    fun `checkRequirements should handle negative version requirement`() {
        // All JVM versions should be >= negative numbers
        assertTrue(checker.checkRequirements("-1"))
    }

    @Test
    fun `checkRequirements should handle string numbers with leading zeros`() {
        assertTrue(checker.checkRequirements("08"))
        assertTrue(checker.checkRequirements("011"))
    }

    @Test
    fun `checkRequirements should handle various valid integer strings`() {
        // Test different valid integer formats
        assertTrue(checker.checkRequirements("1"))
        assertTrue(checker.checkRequirements("8"))
        assertTrue(checker.checkRequirements("11"))
    }

    @Test
    fun `KEY should return correct constant`() {
        assertEquals("required_jvm_version", JvmVersionRequirementChecker.KEY)
    }

    // Helper method to get current Java version for testing
    private fun getCurrentJavaVersionForTest(): Int {
        val version = System.getProperty("java.version")
        val parts = version.split('.')
        val firstPart = parts.first().toInt()

        return if (firstPart == 1) {
            // Legacy format: "1.8.0_292" -> 8
            parts[1].toInt()
        } else {
            // Modern format: "11.0.11" -> 11
            firstPart
        }
    }

    // Integration tests with different scenarios
    @Test
    fun `checkRequirements should work with realistic version scenarios`() {
        // Test common JVM version requirements
        val commonRequirements = listOf("8", "11", "17", "21")
        val currentJavaVersion = getCurrentJavaVersionForTest()

        commonRequirements.forEach { requirement ->
            val requirementInt = requirement.toInt()
            val result = checker.checkRequirements(requirement)
            val expectedResult = currentJavaVersion >= requirementInt

            assertEquals(
                expectedResult,
                result,
                "Failed for requirement $requirement with current Java version $currentJavaVersion"
            )
        }
    }

    @Test
    fun `checkRequirements should handle edge case version numbers`() {
        // Test with very large version numbers (future-proofing)
        assertFalse(checker.checkRequirements("999"))
        assertFalse(checker.checkRequirements("1000"))
    }

    @Test
    fun `checkRequirements should be consistent with multiple calls`() {
        val requirement = "11"
        val result1 = checker.checkRequirements(requirement)
        val result2 = checker.checkRequirements(requirement)
        val result3 = checker.checkRequirements(requirement)

        assertEquals(result1, result2)
        assertEquals(result2, result3)
    }
}
