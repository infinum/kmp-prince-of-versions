package com.infinum.princeofversions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for IosConfigurationParser (String versions; NSJSONSerialization-based).
 */
class IosConfigurationParserTest {

    private fun parser(requirementCheckers: Map<String, RequirementChecker> = defaultCheckers()): IosConfigurationParser {
        return IosConfigurationParser(RequirementsProcessor(requirementCheckers))
    }

    private fun defaultCheckers(): Map<String, RequirementChecker> =
        mapOf(SystemVersionRequirementChecker.KEY to AlwaysTrueRequirementChecker)

    // -------------------- Basics & errors --------------------

    @Test
    fun invalidUpdateNoIosKey() {
        val json = """{ "meta": { "x": "1" } }"""
        val exception = assertFailsWith<IllegalStateException> {
            parser().parse(json)
        }
        assertEquals("Config resource does not contain ios key", exception.message)
    }

    @Test
    fun invalidUpdateNotJson() {
        assertFailsWith<IllegalStateException> {
            parser().parse("not-json at all")
        }
    }

    @Test
    fun malformedJson() {
        assertFailsWith<IllegalStateException> {
            parser().parse("{")
        }
    }

    // -------------------- ios2 (flat) object --------------------

    @Test
    fun ios2_object_full() {
        val json = """
            {
              "ios2": {
                "required_version": "1.2.3",
                "last_version_available": "2.4.5",
                "notify_last_version_frequency": "ALWAYS"
              }
            }
        """.trimIndent()

        val config = parser().parse(json)
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5",
            optionalNotificationType = NotificationType.ALWAYS,
            metadata = emptyMap(),
            requirements = emptyMap(),
        )
        assertEquals(expected, config)
    }

    @Test
    fun ios2_object_missing_notification_defaults_once() {
        val json = """
            {
              "ios2": {
                "required_version": "1.2.3",
                "last_version_available": "2.4.5"
              }
            }
        """.trimIndent()

        val config = parser().parse(json)
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5",
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap(),
        )
        assertEquals(expected, config)
    }

    @Test
    fun ios2_object_invalid_notification_type_throws() {
        val json = """
            { "ios2": { "notify_last_version_frequency": 123 } }
        """.trimIndent()
        assertFailsWith<IllegalArgumentException> {
            parser().parse(json)
        }
    }

    // -------------------- ios2 (flat) array + requirements --------------------

    @Test
    fun ios2_array_picks_first_feasible_by_requirements() {
        // simulated requirement checker will only pass "13" and fail "21"
        val reqChecker = object : RequirementChecker {
            override fun checkRequirements(value: String?): Boolean = (value == "13")
        }
        val checkers = mapOf(SystemVersionRequirementChecker.KEY to reqChecker)

        val json = """
            {
              "ios2": [
                {
                  "required_version": "1.0.0",
                  "last_version_available": "2.0.0",
                  "notify_last_version_frequency": "ONCE",
                  "requirements": { "required_os_version": "21" }
                },
                {
                  "required_version": "1.1.0",
                  "last_version_available": "2.1.0",
                  "notify_last_version_frequency": "ALWAYS",
                  "requirements": { "required_os_version": "13" },
                  "meta": { "k": "v" }
                }
              ]
            }
        """.trimIndent()

        val config = parser(checkers).parse(json)
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = "1.1.0",
            optionalVersion = "2.1.0",
            optionalNotificationType = NotificationType.ALWAYS,
            metadata = mapOf("k" to "v"),
            requirements = mapOf("required_os_version" to "13"),
        )
        assertEquals(expected, config)
    }

    @Test
    fun ios2_array_no_feasible_throws_RequirementsNotSatisfiedException() {
        val reqChecker = object : RequirementChecker {
            override fun checkRequirements(value: String?): Boolean = false
        }
        val checkers = mapOf(SystemVersionRequirementChecker.KEY to reqChecker)

        val json = """
            {
              "ios2": [
                { "requirements": { "required_os_version": "13" } },
                { "requirements": { "required_os_version": "21" } }
              ],
              "meta": { "x": "root" }
            }
        """.trimIndent()

        val extension = assertFailsWith<RequirementsNotSatisfiedException> {
            parser(checkers).parse(json)
        }
        // it should carry root metadata
        assertTrue(extension.metadata["x"] == "root")
    }

    // -------------------- ios (nested) --------------------

    @Test
    fun ios_nested_full() {
        val json = """
            {
              "ios": {
                 "minimum_version": "1.2.3",
                 "latest_version": {
                   "version": "2.4.5",
                   "notification_type": "ALWAYS",
                   "min_sdk": "12.1.2"
                 }
              }
            }
        """.trimIndent()

        val config = parser().parse(json)
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5",
            optionalNotificationType = NotificationType.ALWAYS,
            metadata = emptyMap(),
            requirements = emptyMap(),
        )
        assertEquals(expected, config)
    }

    @Test
    fun ios_nested_missing_notification_defaults_once() {
        val json = """
            {
              "ios": {
                 "minimum_version": "1.2.3",
                 "latest_version": { "version": "2.4.5" }
              }
            }
        """.trimIndent()

        val config = parser().parse(json)
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5",
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap(),
        )
        assertEquals(expected, config)
    }

    // -------------------- ios2 preferred over ios --------------------

    @Test
    fun ios2_preferred_over_ios_when_both_present() {
        val json = """
            {
              "ios2": { "required_version": "9.9.9" },
              "ios": { "minimum_version": "1.2.3",
                       "latest_version": { "version": "2.4.5", "notification_type": "ALWAYS" } }
            }
        """.trimIndent()

        val config = parser().parse(json)
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = "9.9.9",
            optionalVersion = null,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap(),
        )
        assertEquals(expected, config)
    }

    // -------------------- metadata merge tests --------------------

    @Test
    fun ios2_array_root_and_child_metadata_merge() {
        val json = """
          {
            "meta": { "x": "10", "z": "1" },
            "ios2": [
              {
                "required_version": "1",
                "last_version_available": "2",
                "meta": { "z": "3" }
              }
            ]
          }
        """.trimIndent()

        val config = parser().parse(json)
        // root z=1 overridden by child z=3; x preserved
        assertEquals(mapOf("x" to "10", "z" to "3"), config.metadata)
    }

    @Test
    fun ios2_object_child_metadata_only() {
        val json = """
          {
            "ios2": {
              "required_version": "1",
              "last_version_available": "2",
              "meta": { "k1": "v1", "k2": "v2" }
            }
          }
        """.trimIndent()

        val config = parser().parse(json)
        assertEquals(mapOf("k1" to "v1", "k2" to "v2"), config.metadata)
    }

    // -------------------- helpers --------------------

    private object AlwaysTrueRequirementChecker : RequirementChecker {
        override fun checkRequirements(value: String?): Boolean = true
    }
}
