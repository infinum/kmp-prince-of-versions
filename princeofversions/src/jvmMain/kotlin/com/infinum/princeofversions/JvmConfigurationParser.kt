package com.infinum.princeofversions

import org.json.JSONArray
import org.json.JSONObject

/**
 * This interface parses update resource text into [BasePrinceOfVersionsConfig].
 */
public typealias ConfigurationParser = BaseConfigurationParser<String>

/**
 * This class holds loaded data from a configuration resource.
 */
public typealias PrinceOfVersionsConfig = BasePrinceOfVersionsConfig<String>

/**
 * This class represents a parser for update configurations in the JSON format for a JVM environment.
 */
internal class JvmConfigurationParser(private val requirementsProcessor: RequirementsProcessor) : ConfigurationParser {

    private data class ParsedUpdateData(
        val mandatoryVersion: String? = null,
        val optionalVersion: String? = null,
        val optionalNotificationType: NotificationType = NotificationType.ONCE,
        val updateMetadata: Map<String, String> = emptyMap(),
        val requirements: Map<String, String> = emptyMap(),
    )

    override fun parse(value: String): PrinceOfVersionsConfig {
        val data = JSONObject(value)
        return parseRoot(data)
    }

    private fun parseRoot(data: JSONObject): PrinceOfVersionsConfig {
        val rootMeta = data.optJSONObject(META)?.let { jsonObjectToMap(it) } ?: emptyMap()

        val jvmKey = when {
            data.has(JVM_KEY) -> JVM_KEY
            else -> error("Config resource does not contain jvm key")
        }

        return when (val jvmData = data[jvmKey]) {
            is JSONArray -> handleJvmJsonArray(jvmData, rootMeta)
            is JSONObject -> handleJvmJsonObject(jvmData, rootMeta)
            else -> throw IllegalArgumentException("Unsupported type for '$jvmKey' key.")
        }
    }

    private fun handleJvmJsonArray(
        jvm: JSONArray,
        rootMeta: Map<String, String>,
    ): PrinceOfVersionsConfig {
        for (i in 0 until jvm.length()) {
            val update = jvm.getJSONObject(i)
            val parsedData = parseJsonUpdate(update)

            if (parsedData != null) {
                return PrinceOfVersionsConfig(
                    mandatoryVersion = parsedData.mandatoryVersion,
                    optionalVersion = parsedData.optionalVersion,
                    optionalNotificationType = parsedData.optionalNotificationType,
                    requirements = parsedData.requirements,
                    metadata = rootMeta + parsedData.updateMetadata,
                )
            }
        }

        require(jvm.length() > 0) { "JSON doesn't contain any feasible update. Check JSON update format!" }
        throw RequirementsNotSatisfiedException(rootMeta)
    }

    private fun handleJvmJsonObject(
        jvm: JSONObject,
        rootMeta: Map<String, String>,
    ): PrinceOfVersionsConfig {
        val parsedData = parseJsonUpdate(jvm)
            ?: throw RequirementsNotSatisfiedException(rootMeta)

        return PrinceOfVersionsConfig(
            mandatoryVersion = parsedData.mandatoryVersion,
            optionalVersion = parsedData.optionalVersion,
            optionalNotificationType = parsedData.optionalNotificationType,
            requirements = parsedData.requirements,
            metadata = rootMeta + parsedData.updateMetadata,
        )
    }

    private fun parseJsonUpdate(update: JSONObject): ParsedUpdateData? {
        val requirements = parseAndCheckRequirements(update)
            ?: return null

        return ParsedUpdateData(
            mandatoryVersion = parseString(update, MINIMUM_VERSION),
            optionalVersion = parseString(update, LATEST_VERSION),
            optionalNotificationType = parseNotificationType(update),
            updateMetadata = update.optJSONObject(META)?.let { jsonObjectToMap(it) } ?: emptyMap(),
            requirements = requirements,
        )
    }

    private fun parseAndCheckRequirements(update: JSONObject): Map<String, String>? {
        val requirements = update.optJSONObject(REQUIREMENTS)?.let { parseRequirements(it) } ?: emptyMap()
        if (requirements.isNotEmpty() && !requirementsProcessor.areRequirementsSatisfied(requirements)) {
            return null
        }
        return requirements
    }

    private fun parseString(json: JSONObject, key: String): String? {
        if (json.isNull(key)) {
            return null
        }
        return json.optString(key, null)
    }

    private fun parseNotificationType(json: JSONObject): NotificationType {
        if (json.isNull(NOTIFICATION)) {
            return NotificationType.ONCE
        }
        return when (val value = json[NOTIFICATION]) {
            is String -> if (value.equals(NOTIFICATION_ALWAYS, ignoreCase = true)) {
                NotificationType.ALWAYS
            } else {
                NotificationType.ONCE
            }
            else -> throw IllegalArgumentException(
                "In update configuration $NOTIFICATION should be String, but the actual value is $value",
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

    private companion object {
        // JSON Keys
        private const val JVM_KEY = "jvm"
        private const val MINIMUM_VERSION = "required_version"
        private const val LATEST_VERSION = "last_version_available"
        private const val NOTIFICATION = "notify_last_version_frequency"
        private const val META = "meta"
        private const val REQUIREMENTS = "requirements"

        // Notification Values
        private const val NOTIFICATION_ALWAYS = "always"
    }
}
