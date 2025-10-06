package com.infinum.princeofversions

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for PrinceOfVersionsComponents.Builder functionality.
 */
@RunWith(JUnit4::class)
class PrinceOfVersionsComponentsTest {

    private val mockVersionComparator = MockVersionComparator()
    private val mockVersionProvider = MockApplicationVersionProvider()
    private val mockStorage = MockStorage()
    private val mockConfigurationParser = MockConfigurationParser()

    @Test
    fun `withRequirementCheckers should keep default checkers when keepDefaultCheckers is true`() {
        val customChecker = MockRequirementChecker()
        val customCheckers = mapOf("custom_key" to customChecker)

        val components = PrinceOfVersionsComponents.Builder()
            .withVersionProvider(mockVersionProvider)
            .withStorage(mockStorage)
            .withRequirementCheckers(customCheckers, keepDefaultCheckers = true)
            .build()

        // Should contain both default SystemVersionRequirementChecker and custom checker
        assertTrue(components.requirementCheckers.containsKey(SystemVersionRequirementChecker.KEY))
        assertTrue(components.requirementCheckers.containsKey("custom_key"))
        assertEquals(2, components.requirementCheckers.size)
    }

    @Test
    fun `withRequirementCheckers should replace default checkers when keepDefaultCheckers is false`() {
        val customChecker = MockRequirementChecker()
        val customCheckers = mapOf("custom_key" to customChecker)

        val components = PrinceOfVersionsComponents.Builder()
            .withVersionProvider(mockVersionProvider)
            .withStorage(mockStorage)
            .withRequirementCheckers(customCheckers, keepDefaultCheckers = false)
            .build()

        // Should contain only custom checker, no default SystemVersionRequirementChecker
        assertTrue(components.requirementCheckers.containsKey("custom_key"))
        assertEquals(1, components.requirementCheckers.size)
        assertEquals(customChecker, components.requirementCheckers["custom_key"])
    }

    @Test
    fun `build without context should throw when versionProvider is not specified`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            PrinceOfVersionsComponents.Builder()
                .withStorage(mockStorage)
                .build()
        }

        assertEquals(
            "ApplicationVersionProvider is required to build PrinceOfVersionsComponents without context.",
            exception.message
        )
    }

    @Test
    fun `build without context should throw when storage is not specified`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            PrinceOfVersionsComponents.Builder()
                .withVersionProvider(mockVersionProvider)
                .build()
        }

        assertEquals(
            "Storage is required to build PrinceOfVersionsComponents without context.",
            exception.message
        )
    }

    @Test
    fun `build without context should succeed when both versionProvider and storage are specified`() {
        val components = PrinceOfVersionsComponents.Builder()
            .withVersionProvider(mockVersionProvider)
            .withStorage(mockStorage)
            .withVersionComparator(mockVersionComparator)
            .withConfigurationParser(mockConfigurationParser)
            .build()

        assertEquals(mockVersionProvider, components.versionProvider)
        assertEquals(mockStorage, components.storage)
        assertEquals(mockVersionComparator, components.versionComparator)
        assertEquals(mockConfigurationParser, components.configurationParser)
        assertNotNull(components.requirementCheckers)
    }

    @Test
    fun `builder methods should return builder instance for chaining`() {
        val builder = PrinceOfVersionsComponents.Builder()
            .withVersionComparator(mockVersionComparator)
            .withVersionProvider(mockVersionProvider)
            .withStorage(mockStorage)
            .withConfigurationParser(mockConfigurationParser)
            .withRequirementCheckers(emptyMap())

        // If chaining works, this should not throw
        val components = builder.build()
        assertNotNull(components)
    }

    // Mock implementations for testing without external dependencies

    private class MockVersionComparator : VersionComparator {
        override fun compare(firstVersion: Long, secondVersion: Long): Int = 0
    }

    private class MockApplicationVersionProvider : ApplicationVersionProvider {
        override fun getVersion(): Long = 100L
    }

    private class MockStorage : Storage {
        private var savedValue: Long? = null

        override suspend fun saveVersion(version: Long) {
            savedValue = version
        }

        override suspend fun getLastSavedVersion(): Long? = savedValue
    }

    private class MockConfigurationParser : ConfigurationParser {
        override fun parse(value: String): PrinceOfVersionsConfig {
            return PrinceOfVersionsConfig(
                mandatoryVersion = null,
                optionalVersion = null,
                optionalNotificationType = NotificationType.ALWAYS,
                metadata = emptyMap(),
                requirements = emptyMap()
            )
        }
    }

    private class MockRequirementChecker : RequirementChecker {
        override fun checkRequirements(value: String?): Boolean = true
    }
}
