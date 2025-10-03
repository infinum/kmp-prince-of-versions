package com.infinum.princeofversions

import com.infinum.princeofversions.util.ResourceUtils
import org.json.JSONException
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@RunWith(JUnit4::class)
class AndroidConfigurationParserTest {

    private lateinit var parser: AndroidConfigurationParser

    @Before
    fun setUp() {
        val defaultRequirements: Map<String, RequirementChecker> = mapOf(
            SystemVersionRequirementChecker.KEY to MockSystemVersionRequirementChecker(21)
        )
        parser = AndroidConfigurationParser(RequirementsProcessor(defaultRequirements))
    }

    @Test
    fun checkEmptyJsonToStringMap() {
        assertTrue(parser.jsonObjectToMap(JSONObject("{}")).isEmpty())
    }

    @Test
    fun checkJsonToStringMap() {
        val map = parser.jsonObjectToMap(JSONObject(ResourceUtils.readFromFile("json_obj_string.json")))
        assertEquals(mapOf("key1" to "value1", "key2" to "value2"), map)
    }

    @Test
    fun checkJsonToStringMapWithNull() {
        val map = parser.jsonObjectToMap(JSONObject(ResourceUtils.readFromFile("json_obj_string_with_null.json")))
        assertEquals(mapOf("key1" to null, "key2" to "value2"), map)
    }

    @Test
    fun checkComplexJsonToStringMap() {
        val map = parser.jsonObjectToMap(JSONObject(ResourceUtils.readFromFile("json_obj_string_complex.json")))
        assertEquals(
            mapOf(
                "key1" to "value1",
                "key2" to "value2",
                "key3" to "true",
                "key4" to "0",
                "key5" to "[0,1]",
                "key6" to "{}"
            ), map
        )
    }

    @Test
    fun invalidUpdateNoAndroidKey() {
        val exception = assertFailsWith<IllegalStateException> {
            parser.parse(ResourceUtils.readFromFile("invalid_update_no_android.json"))
        }
        assertEquals("Config resource does not contain android key", exception.message)
    }

    @Test
    fun invalidUpdateNotJson() {
        assertFailsWith<JSONException> {
            parser.parse(ResourceUtils.readFromFile("invalid_update_no_json.json"))
        }
    }

    @Test
    fun malformedJson() {
        assertFailsWith<JSONException> {
            parser.parse(ResourceUtils.readFromFile("malformed_json.json"))
        }
    }

    @Test
    fun validUpdateFullJson() {
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_full.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 123L,
            optionalVersion = 245L,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun validUpdateFullWithMetadataJson() {
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_full_with_metadata.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 123L,
            optionalVersion = 245L,
            optionalNotificationType = NotificationType.ONCE,
            metadata = mapOf("key1" to "value1", "key2" to "value2"),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun validUpdateFullWithEmptyMetadataJson() {
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_full_with_metadata_empty.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 123L,
            optionalVersion = 245L,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun validUpdateFullWithMetadataMalformed() {
        assertFailsWith<JSONException> {
            parser.parse(ResourceUtils.readFromFile("valid_update_full_with_metadata_malformed.json"))
        }
    }

    @Test
    fun validUpdateFullWithNullMetadataJson() {
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_full_with_metadata_null.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 123L,
            optionalVersion = 245L,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun validUpdateFullWithSdkValuesJson() {
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_full_with_sdk_values.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 123L,
            optionalVersion = 240L,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = mapOf("required_os_version" to "17")
        )
        assertEquals(expected, config)
    }

    @Test
    fun noMandatoryVersionJson() {
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_no_min_version.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = null,
            optionalVersion = 245L,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun validUpdateNoNotificationJson() {
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_no_notification.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 123L,
            optionalVersion = 245L,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun validUpdateAlwaysNotificationJson() {
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_notification_always.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 123L,
            optionalVersion = 245L,
            optionalNotificationType = NotificationType.ALWAYS,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun mandatoryVersionNullJson() {
        val result = parser.parse(ResourceUtils.readFromFile("valid_update_null_min_version.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = null,
            optionalVersion = 245L,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
        assertEquals(expected, result)
    }

    @Test
    fun validUpdateOnlyMandatoryJson() {
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_only_min_version.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 123L,
            optionalVersion = null,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun validUpdateWithJsonArray() {
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_full_array.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 123L,
            optionalVersion = 245L,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun validUpdateWithMergingMetadata() {
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_full_array_with_metadata.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 123L,
            optionalVersion = 245L,
            optionalNotificationType = NotificationType.ONCE,
            metadata = mapOf("x" to "10", "z" to "3"),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun validUpdateWithOverridingMetadata() {
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_full_array_with_overriding_metadata.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 123L,
            optionalVersion = 245L,
            optionalNotificationType = NotificationType.ONCE,
            metadata = mapOf("x" to "10"),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun invalidUpdateWithStringLatestVersion() {
        assertFailsWith<IllegalArgumentException> {
            parser.parse(ResourceUtils.readFromFile("invalid_update_with_string_version.json"))
        }
    }

    @Test
    fun invalidUpdateWithIntNotification() {
        assertFailsWith<IllegalArgumentException> {
            parser.parse(ResourceUtils.readFromFile("invalid_update_with_int_notification_type.json"))
        }
    }

    @Test
    fun validUpdateWithRequirements() {
        val checker = MockSystemVersionRequirementChecker(13)
        val requirements: Map<String, RequirementChecker> = mapOf(SystemVersionRequirementChecker.KEY to checker)
        val processor = RequirementsProcessor(requirements)
        val parser = AndroidConfigurationParser(processor)
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_full_array_with_requirements.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 123L,
            optionalVersion = 246L,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = mapOf("required_os_version" to "13")
        )
        assertEquals(expected, config)
    }

    @Test
    fun invalidDataInDefaultRequirementChecker() {
        val checker = MockSystemVersionRequirementChecker(13)
        assertFailsWith<NumberFormatException> {
            checker.checkRequirements("not integer")
        }
    }

    @Test
    fun checkAndroid2KeyIsUsedOverAndroidKey() {
        val json = """
            {
                "android2": { "required_version": 999 },
                "android": { "required_version": 123 }
            }
        """.trimIndent()
        val config = parser.parse(json)
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 999L, // Version from 'android2'
            optionalVersion = null,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun checkAndroidKeyIsUsedAsFallback() {
        val json = """
            {
                "android": { "required_version": 123 }
            }
        """.trimIndent()
        val config = parser.parse(json)
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = 123L, // Version from 'android'
            optionalVersion = null,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun checkRequirementsNotSatisfiedInArrayThrowsException() {
        val checker = MockSystemVersionRequirementChecker(10) // Device SDK is 10
        val requirements: Map<String, RequirementChecker> = mapOf(SystemVersionRequirementChecker.KEY to checker)
        val processor = RequirementsProcessor(requirements)
        val parser = AndroidConfigurationParser(processor)
        // JSON requires SDK 13 and 21, neither is satisfied
        assertFailsWith<RequirementsNotSatisfiedException> {
            parser.parse(ResourceUtils.readFromFile("valid_update_full_array_with_requirements.json"))
        }
    }

    private class MockSystemVersionRequirementChecker(
        private val deviceSdkVersion: Int
    ) : RequirementChecker {

        override fun checkRequirements(value: String?): Boolean {
            val minSdk = value?.toInt() ?: return false
            return minSdk <= deviceSdkVersion
        }
    }
}
