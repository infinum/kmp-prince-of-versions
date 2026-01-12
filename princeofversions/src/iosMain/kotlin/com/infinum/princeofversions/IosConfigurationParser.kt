@file:OptIn(BetaInteropApi::class)

package com.infinum.princeofversions

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSArray
import platform.Foundation.NSDictionary
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSNull
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding

// String versions on iOS (same as JVM)
public typealias ConfigurationParser = BaseConfigurationParser<String>
public typealias PrinceOfVersionsConfig = BasePrinceOfVersionsConfig<String>

/**
 * iOS parser using Foundation's NSJSONSerialization.
 * Prefers "ios2" (flat), falls back to "ios" (nested).
 */
@OptIn(ExperimentalForeignApi::class)
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
        val data = value.toNSDataUtf8()
            ?: throw ConfigurationException("Invalid UTF-8 input")
        val rootAny = NSJSONSerialization.JSONObjectWithData(data, 0uL, null)
            ?: throw ConfigurationException("Invalid JSON")
        val root = rootAny as? NSDictionary
            ?: throw ConfigurationException("Root JSON must be an object")
        return parseRoot(root)
    }

    private fun parseRoot(data: NSDictionary): PrinceOfVersionsConfig {
        val rootMeta = data.optDict(META)?.toStringMap() ?: emptyMap()

        val iosKey = when {
            data.hasKey(IOS2_KEY) -> IOS2_KEY
            data.hasKey(IOS_KEY) -> IOS_KEY
            else -> throw ConfigurationException("Config resource does not contain ios key")
        }

        val iosData = data.opt(iosKey)
        return when (iosData) {
            is NSArray -> handleIos2Array(iosData, rootMeta)
            is NSDictionary -> if (iosKey == IOS_KEY) {
                handleNestedIosObject(iosData, rootMeta)
            } else {
                handleIos2Object(iosData, rootMeta)
            }
            else -> throw IllegalArgumentException("Unsupported type for '$iosKey' key.")
        }
    }

    // ----- ios2 (flat) -----

    private fun handleIos2Array(arr: NSArray, rootMeta: Map<String, String>): PrinceOfVersionsConfig {
        val n = arr.count.toInt()
        for (i in 0 until n) {
            val update = arr.objectAtIndex(i.toULong()) as? NSDictionary ?: continue
            val parsed = parseFlatUpdate(update)
            if (parsed != null) {
                return PrinceOfVersionsConfig(
                    mandatoryVersion = parsed.mandatoryVersion,
                    optionalVersion = parsed.optionalVersion,
                    optionalNotificationType = parsed.optionalNotificationType,
                    requirements = parsed.requirements,
                    metadata = rootMeta + parsed.updateMetadata,
                )
            }
        }
        if (n == 0) {
            throw ConfigurationException("JSON doesn't contain any feasible update. Check JSON update format!")
        }
        throw RequirementsNotSatisfiedException(rootMeta)
    }

    private fun handleIos2Object(obj: NSDictionary, rootMeta: Map<String, String>): PrinceOfVersionsConfig {
        val parsed = parseFlatUpdate(obj) ?: throw RequirementsNotSatisfiedException(rootMeta)
        return PrinceOfVersionsConfig(
            mandatoryVersion = parsed.mandatoryVersion,
            optionalVersion = parsed.optionalVersion,
            optionalNotificationType = parsed.optionalNotificationType,
            requirements = parsed.requirements,
            metadata = rootMeta + parsed.updateMetadata,
        )
    }

    private fun parseFlatUpdate(update: NSDictionary): ParsedUpdateData? {
        val reqs = parseAndCheckRequirements(update) ?: return null
        return ParsedUpdateData(
            mandatoryVersion = update.string(MINIMUM_VERSION),
            optionalVersion = update.string(LATEST_VERSION),
            optionalNotificationType = parseFlatNotificationType(update),
            updateMetadata = update.optDict(META)?.toStringMap() ?: emptyMap(),
            requirements = reqs,
        )
    }

    private fun parseFlatNotificationType(json: NSDictionary): NotificationType {
        val raw = json.objectForKey(NOTIFY_FLAT) ?: return NotificationType.ONCE
        if (raw is NSNull) return NotificationType.ONCE

        if (raw !is NSString) {
            throw ConfigurationException("In update configuration $NOTIFY_FLAT should be String, but the actual value is $raw")
        }
        return if (raw.toString().equals(NOTIFY_ALWAYS, ignoreCase = true)) {
            NotificationType.ALWAYS
        } else {
            NotificationType.ONCE
        }
    }

    // ----- ios (nested) -----

    private fun handleNestedIosObject(obj: NSDictionary, rootMeta: Map<String, String>): PrinceOfVersionsConfig {
        val parsed = parseNestedUpdate(obj) ?: throw RequirementsNotSatisfiedException(rootMeta)
        return PrinceOfVersionsConfig(
            mandatoryVersion = parsed.mandatoryVersion,
            optionalVersion = parsed.optionalVersion,
            optionalNotificationType = parsed.optionalNotificationType,
            requirements = parsed.requirements,
            metadata = rootMeta + parsed.updateMetadata,
        )
    }

    private fun parseNestedUpdate(ios: NSDictionary): ParsedUpdateData? {
        val minimumVersion = ios.string("minimum_version")
        val latest = ios.optDict("latest_version")
        val latestVersion = latest?.string("version")
        val notificationType = latest?.string("notification_type")

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

    private fun parseAndCheckRequirements(update: NSDictionary): Map<String, String>? {
        val reqs = update.optDict(REQUIREMENTS)?.toStringMap() ?: emptyMap()
        if (reqs.isNotEmpty() && !requirementsProcessor.areRequirementsSatisfied(reqs)) return null
        return reqs
    }

    // NSDictionary helpers
    private fun NSDictionary.hasKey(key: String): Boolean = this.objectForKey(key) != null

    private fun NSDictionary.opt(key: String): Any? {
        val v = this.objectForKey(key)
        return if (v is NSNull) null else v
    }

    private fun NSDictionary.optDict(key: String): NSDictionary? = opt(key) as? NSDictionary

    private fun NSDictionary.string(key: String): String? {
        val v = opt(key) ?: return null
        return v.toString()
    }

    private fun NSDictionary.toStringMap(): Map<String, String> {
        val dict = this
        val out = mutableMapOf<String, String>()
        val enumerator = dict.keyEnumerator()
        var key = enumerator.nextObject()
        while (key != null) {
            val value = dict.objectForKey(key)
            if (value != null && value !is NSNull) out[key.toString()] = value.toString()
            key = enumerator.nextObject()
        }
        return out
    }

    private fun String.toNSDataUtf8() =
        NSString.create(string = this).dataUsingEncoding(NSUTF8StringEncoding)

    private companion object {
        private const val IOS2_KEY = "ios2" // flat
        private const val IOS_KEY = "ios" // nested
        private const val MINIMUM_VERSION = "required_version"
        private const val LATEST_VERSION = "last_version_available"
        private const val NOTIFY_FLAT = "notify_last_version_frequency"
        private const val REQUIREMENTS = "requirements"
        private const val META = "meta"
        private const val NOTIFY_ALWAYS = "ALWAYS"
    }
}
