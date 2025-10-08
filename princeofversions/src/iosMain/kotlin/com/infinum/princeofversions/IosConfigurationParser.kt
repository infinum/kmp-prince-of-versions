package com.infinum.princeofversions

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

// iOS source set typealiases (String-based versions, like JVM)
public typealias ConfigurationParser = BaseConfigurationParser<String>
public typealias PrinceOfVersionsConfig = BasePrinceOfVersionsConfig<String>

/**
 * KMP iOS parser using kotlinx-serialization-json.
 * Prefers "ios2" (flat) and falls back to "ios" (nested).
 */
internal class IosConfigurationParser(
    private val requirementsProcessor: RequirementsProcessor,
) : ConfigurationParser {

    private data class ParsedUpdateData(
        val mandatoryVersion: String? = null,
        val optionalVersion: String? = null,
        val optionalNotificationType: NotificationType = NotificationType.ONCE,
        val updateMetadata: Map<String, String> = emptyMap(),
        val requirements: Map<String, String> = emptyMap(),
    )

    override fun parse(value: String): PrinceOfVersionsConfig {
        val data = Json.parseToJsonElement(value).jsonObject
        return parseRoot(data)
    }

    private fun parseRoot(data: JsonObject): PrinceOfVersionsConfig {
        val rootMeta = data[META]?.jsonObject?.let { jsonObjectToMap(it) } ?: emptyMap()

        val iosKey = when {
            data.containsKey(IOS2_KEY) -> IOS2_KEY
            data.containsKey(IOS_KEY) -> IOS_KEY
            else -> error("Config resource does not contain ios key")
        }

        val iosData = data[iosKey]
        return when (iosData) {
            is JsonArray -> handleIos2Array(iosData, rootMeta)
            is JsonObject ->
                if (iosKey == IOS_KEY) handleNestedIosObject(iosData, rootMeta)
                else handleIos2Object(iosData, rootMeta)

            else -> throw IllegalArgumentException("Unsupported type for '$iosKey' key.")
        }
    }

    // ----- ios2 (flat) -----

    private fun handleIos2Array(
        arr: JsonArray,
        rootMeta: Map<String, String>
    ): PrinceOfVersionsConfig {
        for (el in arr) {
            val obj = el as? JsonObject ?: continue
            val parsed = parseFlatUpdate(obj)
            if (parsed != null) {
                return PrinceOfVersionsConfig(
                    mandatoryVersion = parsed.mandatoryVersion,
                    optionalVersion = parsed.optionalVersion,
                    optionalNotificationType = parsed.optionalNotificationType,
                    requirements = parsed.requirements,
                    metadata = rootMeta + parsed.updateMetadata
                )
            }
        }
        require(arr.isNotEmpty()) { "JSON doesn't contain any feasible update. Check JSON update format!" }
        throw RequirementsNotSatisfiedException(rootMeta)
    }

    private fun handleIos2Object(
        obj: JsonObject,
        rootMeta: Map<String, String>
    ): PrinceOfVersionsConfig {
        val parsed = parseFlatUpdate(obj) ?: throw RequirementsNotSatisfiedException(rootMeta)
        return PrinceOfVersionsConfig(
            mandatoryVersion = parsed.mandatoryVersion,
            optionalVersion = parsed.optionalVersion,
            optionalNotificationType = parsed.optionalNotificationType,
            requirements = parsed.requirements,
            metadata = rootMeta + parsed.updateMetadata
        )
    }

    private fun parseFlatUpdate(update: JsonObject): ParsedUpdateData? {
        val reqs = parseAndCheckRequirements(update) ?: return null
        return ParsedUpdateData(
            mandatoryVersion = parseString(update, MINIMUM_VERSION),
            optionalVersion = parseString(update, LATEST_VERSION),
            optionalNotificationType = parseFlatNotificationType(update),
            updateMetadata = update[META]?.jsonObject?.let { jsonObjectToMap(it) } ?: emptyMap(),
            requirements = reqs
        )
    }

    private fun parseFlatNotificationType(json: JsonObject): NotificationType {
        val v = json[NOTIFY_FLAT]?.jsonPrimitive?.contentOrNull ?: return NotificationType.ONCE
        return if (v.equals(
                NOTIFY_ALWAYS,
                ignoreCase = true
            )
        ) NotificationType.ALWAYS else NotificationType.ONCE
    }

    // ----- ios (nested) -----

    private fun handleNestedIosObject(
        obj: JsonObject,
        rootMeta: Map<String, String>
    ): PrinceOfVersionsConfig {
        val parsed = parseNestedUpdate(obj) ?: throw RequirementsNotSatisfiedException(rootMeta)
        return PrinceOfVersionsConfig(
            mandatoryVersion = parsed.mandatoryVersion,
            optionalVersion = parsed.optionalVersion,
            optionalNotificationType = parsed.optionalNotificationType,
            requirements = parsed.requirements,
            metadata = rootMeta + parsed.updateMetadata
        )
    }

    private fun parseNestedUpdate(ios: JsonObject): ParsedUpdateData? {
        val minimumVersion = ios["minimum_version"]?.jsonPrimitive?.contentOrNull
        val latest = ios["latest_version"]?.jsonObject
        val latestVersion = latest?.get("version")?.jsonPrimitive?.contentOrNull
        val notificationType = latest?.get("notification_type")?.jsonPrimitive?.contentOrNull

        return ParsedUpdateData(
            mandatoryVersion = minimumVersion,
            optionalVersion = latestVersion,
            optionalNotificationType = when {
                notificationType == null -> NotificationType.ONCE
                notificationType.equals(NOTIFY_ALWAYS, ignoreCase = true) -> NotificationType.ALWAYS
                else -> NotificationType.ONCE
            },
            updateMetadata = emptyMap(),
            requirements = emptyMap(),
        )
    }

    // ----- shared helpers -----

    private fun parseAndCheckRequirements(update: JsonObject): Map<String, String>? {
        val reqs = update[REQUIREMENTS]?.jsonObject?.let { parseRequirements(it) } ?: emptyMap()
        if (reqs.isNotEmpty() && !requirementsProcessor.areRequirementsSatisfied(reqs)) return null
        return reqs
    }

    private fun parseString(obj: JsonObject, key: String): String? =
        obj[key]?.jsonPrimitive?.contentOrNull

    private fun parseRequirements(obj: JsonObject): Map<String, String> =
        buildMap {
            for ((k, v) in obj) {
                if (v !is JsonNull) put(k, v.jsonPrimitive.contentOrNull ?: v.toString())
            }
        }

    internal fun jsonObjectToMap(obj: JsonObject?): Map<String, String> =
        buildMap {
            if (obj == null) return@buildMap
            for ((k, v) in obj) {
                if (v !is JsonNull) put(k, v.jsonPrimitive.contentOrNull ?: v.toString())
            }
        }

    private companion object {
        private const val IOS2_KEY = "ios2"       // flat
        private const val IOS_KEY = "ios"         // nested
        private const val MINIMUM_VERSION = "required_version"
        private const val LATEST_VERSION = "last_version_available"
        private const val NOTIFY_FLAT = "notify_last_version_frequency"
        private const val REQUIREMENTS = "requirements"
        private const val META = "meta"
        private const val NOTIFY_ALWAYS = "ALWAYS"
    }
}
