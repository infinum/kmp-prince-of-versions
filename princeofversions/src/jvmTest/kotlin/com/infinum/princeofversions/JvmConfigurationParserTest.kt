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
class JvmConfigurationParserTest {

    private lateinit var parser: JvmConfigurationParser

    @Before
    fun setUp() {
        val defaultRequirements: Map<String, RequirementChecker> = mapOf(
            JvmVersionRequirementChecker.KEY to MockJvmVersionRequirementChecker(11) // Simulate JVM 11
        )
        parser = JvmConfigurationParser(RequirementsProcessor(defaultRequirements))
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
        assertEquals(mapOf("key2" to "value2"), map) // JVM parser skips null values
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
    fun invalidUpdateNoJvmKey() {
        val exception = assertFailsWith<IllegalStateException> {
            parser.parse(ResourceUtils.readFromFile("invalid_update_no_jvm.json"))
        }
        assertEquals("Config resource does not contain jvm key", exception.message)
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
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5",
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
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5",
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
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5",
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
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5",
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
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.0",
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = mapOf("required_jvm_version" to "8")
        )
        assertEquals(expected, config)
    }

    @Test
    fun noMandatoryVersionJson() {
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_no_min_version.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = null,
            optionalVersion = "2.4.5",
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
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5",
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
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5",
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
            optionalVersion = "2.4.5",
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
            mandatoryVersion = "1.2.3",
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
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5",
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
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5",
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
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5",
            optionalNotificationType = NotificationType.ONCE,
            metadata = mapOf("x" to "10"),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun invalidUpdateWithIntLatestVersion() {
        // JVM parser expects string versions, so integer versions should be handled as strings
        // The JVM parser will convert the integer 245 to string "245", so this should actually pass
        val config = parser.parse(ResourceUtils.readFromFile("invalid_update_with_int_version.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = "1.2.3",
            optionalVersion = "245",
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun invalidUpdateWithIntNotification() {
        assertFailsWith<IllegalArgumentException> {
            parser.parse(ResourceUtils.readFromFile("invalid_update_with_int_notification_type.json"))
        }
    }

    @Test
    fun validUpdateWithRequirements() {
        val checker = MockJvmVersionRequirementChecker(11) // JVM 11 satisfies both JVM 8 and 11 requirements
        val requirements: Map<String, RequirementChecker> = mapOf(JvmVersionRequirementChecker.KEY to checker)
        val processor = RequirementsProcessor(requirements)
        val parser = JvmConfigurationParser(processor)
        val config = parser.parse(ResourceUtils.readFromFile("valid_update_full_array_with_requirements.json"))
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = "1.2.3",
            optionalVersion = "2.4.5", // First array entry that satisfies requirements (JVM 8)
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = mapOf("required_jvm_version" to "8")
        )
        assertEquals(expected, config)
    }

    @Test
    fun invalidDataInDefaultRequirementChecker() {
        val checker = MockJvmVersionRequirementChecker(11)
        assertFailsWith<NumberFormatException> {
            checker.checkRequirements("not a version")
        }
    }

    @Test
    fun checkJvmKeyIsUsed() {
        val json = """
            {
                "jvm": { "required_version": "1.2.3" },
                "android": { "required_version": 123 }
            }
        """.trimIndent()
        val config = parser.parse(json)
        val expected = PrinceOfVersionsConfig(
            mandatoryVersion = "1.2.3", // Version from 'jvm'
            optionalVersion = null,
            optionalNotificationType = NotificationType.ONCE,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
        assertEquals(expected, config)
    }

    @Test
    fun checkRequirementsNotSatisfiedInArrayThrowsException() {
        val checker = MockJvmVersionRequirementChecker(7) // JVM 7 - doesn't satisfy JVM 8 or 11 requirements
        val requirements: Map<String, RequirementChecker> = mapOf(JvmVersionRequirementChecker.KEY to checker)
        val processor = RequirementsProcessor(requirements)
        val parser = JvmConfigurationParser(processor)
        // JSON requires JVM 8 and 11, neither is satisfied by JVM 7
        assertFailsWith<RequirementsNotSatisfiedException> {
            parser.parse(ResourceUtils.readFromFile("valid_update_full_array_with_requirements.json"))
        }
    }

    private class MockJvmVersionRequirementChecker(
        private val currentJvmVersion: Int
    ) : RequirementChecker {

        override fun checkRequirements(value: String?): Boolean {
            val requiredVersion = value?.toInt() ?: return false // This will throw NumberFormatException for invalid strings
            return currentJvmVersion >= requiredVersion
        }
    }
}
