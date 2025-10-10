package com.infinum.princeofversions

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for JVM PrinceOfVersionsComponents.Builder functionality.
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

        // Should contain both default JvmVersionRequirementChecker and custom checker
        assertTrue(components.requirementCheckers.containsKey(JvmVersionRequirementChecker.KEY))
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

        // Should contain only custom checker, no default JvmVersionRequirementChecker
        assertTrue(components.requirementCheckers.containsKey("custom_key"))
        assertEquals(1, components.requirementCheckers.size)
        assertEquals(customChecker, components.requirementCheckers["custom_key"])
    }

    @Test
    fun `build without mainClass should throw when storage is not specified`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            PrinceOfVersionsComponents.Builder()
                .withVersionProvider(mockVersionProvider)
                .build()
        }

        assertEquals(
            "Storage is required to build PrinceOfVersionsComponents without the mainClass parameter.",
            exception.message
        )
    }

    @Test
    fun `build without mainClass should succeed when storage is specified`() {
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
    fun `build with mainClass should succeed and create JvmStorage`() {
        val mainClass = PrinceOfVersionsComponentsTest::class.java

        val components = PrinceOfVersionsComponents.Builder()
            .withVersionProvider(mockVersionProvider)
            .withVersionComparator(mockVersionComparator)
            .withConfigurationParser(mockConfigurationParser)
            .build(mainClass)

        assertEquals(mockVersionProvider, components.versionProvider)
        assertEquals(mockVersionComparator, components.versionComparator)
        assertEquals(mockConfigurationParser, components.configurationParser)
        assertNotNull(components.storage)
        assertNotNull(components.requirementCheckers)
        // Storage should be JvmStorage instance (we can't check type directly due to internal visibility)
        assertTrue(components.storage.javaClass.simpleName == "JvmStorage")
    }

    @Test
    fun `build with mainClass should use default components when not specified`() {
        val mainClass = PrinceOfVersionsComponentsTest::class.java

        val components = PrinceOfVersionsComponents.Builder()
            .build(mainClass)

        // Should use default PropertiesApplicationVersionProvider
        assertTrue(components.versionProvider.javaClass.simpleName == "PropertiesApplicationVersionProvider")

        // Should use default JvmDefaultVersionComparator
        assertTrue(components.versionComparator.javaClass.simpleName == "JvmDefaultVersionComparator")

        // Should use default JvmConfigurationParser
        assertTrue(components.configurationParser.javaClass.simpleName == "JvmConfigurationParser")

        // Should contain default JvmVersionRequirementChecker
        assertTrue(components.requirementCheckers.containsKey(JvmVersionRequirementChecker.KEY))

        // Should create JvmStorage
        assertTrue(components.storage.javaClass.simpleName == "JvmStorage")
    }

    @Test
    fun `builder methods should return builder instance for chaining`() {
        val mainClass = PrinceOfVersionsComponentsTest::class.java

        val builder = PrinceOfVersionsComponents.Builder()
            .withVersionComparator(mockVersionComparator)
            .withVersionProvider(mockVersionProvider)
            .withStorage(mockStorage)
            .withConfigurationParser(mockConfigurationParser)
            .withRequirementCheckers(emptyMap())

        // If chaining works, this should not throw
        val components = builder.build(mainClass)
        assertNotNull(components)
    }

    // Mock implementations for testing without external dependencies

    private class MockVersionComparator : VersionComparator {
        override fun compare(firstVersion: String, secondVersion: String): Int = 0
    }

    private class MockApplicationVersionProvider : ApplicationVersionProvider {
        override fun getVersion(): String = "1.0.0"
    }

    private class MockStorage : Storage {
        private var savedValue: String? = null

        override suspend fun saveVersion(version: String) {
            savedValue = version
        }

        override suspend fun getLastSavedVersion(): String? = savedValue
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
