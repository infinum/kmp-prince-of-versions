package com.infinum.princeofversions

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * This class parses update resource text into [PrinceOfVersionsConfig].
 */
public typealias ConfigurationParser = BaseConfigurationParser<Long>

/**
 * This class holds loaded data from a configuration resource.
 */
public typealias PrinceOfVersionsConfig = BasePrinceOfVersionsConfig<Long>

/**
 * This class represents a parser for update configurations in the JSON format.
 *
 * It parses the JSON content and creates a [PrinceOfVersionsConfig] instance.
 */
internal class AndroidConfigurationParser(
    private val requirementsProcessor: RequirementsProcessor,
) : ConfigurationParser {

    private companion object {
        // JSON Keys
        private const val ANDROID_FALLBACK_KEY = "android"
        private const val ANDROID_KEY = "android2"
        private const val MINIMUM_VERSION = "required_version"
        private const val LATEST_VERSION = "last_version_available"
        private const val NOTIFICATION = "notify_last_version_frequency"
        private const val META = "meta"
        private const val REQUIREMENTS = "requirements"

        // Notification Values
        private const val NOTIFICATION_ALWAYS = "always"
    }

    /**
     * A private data class to hold the parsed values from a single update entry before
     * creating the final PrinceOfVersionsConfig.
     */
    private data class ParsedUpdateData(
        val mandatoryVersion: Long? = null,
        val optionalVersion: Long? = null,
        val optionalNotificationType: NotificationType = NotificationType.ONCE,
        val updateMetadata: Map<String, String> = emptyMap(),
        val requirements: Map<String, String> = emptyMap()
    )

    override fun parse(value: String): PrinceOfVersionsConfig {
        val data = JSONObject(value)
        return parseRoot(data)
    }

    private fun parseRoot(data: JSONObject): PrinceOfVersionsConfig {
        val rootMeta = data.optJSONObject(META)?.let { jsonObjectToMap(it) } ?: emptyMap()

        val androidKey = when {
            data.has(ANDROID_KEY) -> ANDROID_KEY
            data.has(ANDROID_FALLBACK_KEY) -> ANDROID_FALLBACK_KEY
            else -> error("Config resource does not contain android key")
        }

        return when (val androidData = data[androidKey]) {
            is JSONArray -> handleAndroidJsonArray(androidData, rootMeta)
            is JSONObject -> handleAndroidJsonObject(androidData, rootMeta)
            else -> throw IllegalArgumentException("Unsupported type for '$androidKey' key.")
        }
    }

    private fun handleAndroidJsonArray(
        android: JSONArray,
        rootMeta: Map<String, String>
    ): PrinceOfVersionsConfig {
        for (i in 0 until android.length()) {
            val update = android.getJSONObject(i)
            val parsedData = parseJsonUpdate(update)

            if (parsedData != null) {
                // Found the first feasible update, construct config and return
                return PrinceOfVersionsConfig(
                    mandatoryVersion = parsedData.mandatoryVersion,
                    optionalVersion = parsedData.optionalVersion,
                    optionalNotificationType = parsedData.optionalNotificationType,
                    requirements = parsedData.requirements,
                    metadata = rootMeta + parsedData.updateMetadata
                )
            }
        }

        // If loop finishes, no feasible update was found.
        require(android.length() > 0) { "JSON doesn't contain any feasible update. Check JSON update format!" }
        throw RequirementsNotSatisfiedException(rootMeta)
    }

    private fun handleAndroidJsonObject(
        android: JSONObject,
        rootMeta: Map<String, String>
    ): PrinceOfVersionsConfig {
        val parsedData = parseJsonUpdate(android)
            ?: throw RequirementsNotSatisfiedException(rootMeta)

        return PrinceOfVersionsConfig(
            mandatoryVersion = parsedData.mandatoryVersion,
            optionalVersion = parsedData.optionalVersion,
            optionalNotificationType = parsedData.optionalNotificationType,
            requirements = parsedData.requirements,
            metadata = rootMeta + parsedData.updateMetadata
        )
    }

    private fun parseJsonUpdate(update: JSONObject): ParsedUpdateData? {
        val requirements = parseAndCheckRequirements(update)
            ?: return null // Requirements not met, skip this update entry

        val mandatoryVersion = parseLong(update, MINIMUM_VERSION)
        val optionalVersion = parseLong(update, LATEST_VERSION)
        val notificationType = parseNotificationType(update)
        val updateMetadata = update.optJSONObject(META)?.let { jsonObjectToMap(it) } ?: emptyMap()

        return ParsedUpdateData(
            mandatoryVersion = mandatoryVersion,
            optionalVersion = optionalVersion,
            optionalNotificationType = notificationType,
            updateMetadata = updateMetadata,
            requirements = requirements
        )
    }

    private fun parseAndCheckRequirements(update: JSONObject): Map<String, String>? {
        val requirements = update.optJSONObject(REQUIREMENTS)?.let { parseRequirements(it) } ?: emptyMap()
        if (requirements.isNotEmpty() && !requirementsProcessor.areRequirementsSatisfied(requirements)) {
            return null
        }
        return requirements
    }

    private fun parseLong(json: JSONObject, key: String): Long? {
        if (json.isNull(key)) {
            return null
        }
        try {
            return json.getLong(key)
        } catch (e: JSONException) {
            throw IllegalArgumentException(
                "In update configuration $key should be Long, but the actual value is ${json[key]}. $e"
            )
        }
    }

    private fun parseNotificationType(json: JSONObject): NotificationType {
        val key = NOTIFICATION
        if (json.isNull(key)) {
            return NotificationType.ONCE
        }
        return when (val value = json[key]) {
            is String -> if (value.lowercase() == NOTIFICATION_ALWAYS.lowercase()) {
                NotificationType.ALWAYS
            } else {
                NotificationType.ONCE
            }
            else -> throw IllegalArgumentException(
                "In update configuration $key should be String, but the actual value is $value"
            )
        }
    }

    private fun parseRequirements(requirementsJson: JSONObject): Map<String, String> {
        val requirements = mutableMapOf<String, String>()
        for (key in requirementsJson.keys()) {
            if (!requirementsJson.isNull(key)) {
                requirements[key] = requirementsJson[key].toString()
            }
        }
        return requirements
    }

    internal fun jsonObjectToMap(jsonObject: JSONObject?): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (jsonObject == null) return map
        for (key in jsonObject.keys()) {
            if (!jsonObject.isNull(key)) {
                map[key] = jsonObject[key].toString()
            }
        }
        return map
    }
}
