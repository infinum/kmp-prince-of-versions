package com.infinum.princeofversions.sample

import com.infinum.princeofversions.ConfigurationParser
import com.infinum.princeofversions.enums.NotificationType
import com.infinum.princeofversions.models.PrinceOfVersionsConfig
import org.json.JSONObject

/**
 * A JVM-specific custom parser that adapts the common logic to work with the <String>
 * generic type used by the desktop components.
 */
class JvmCustomParser : ConfigurationParser<String> {
    companion object {
        private const val MINIMUM_VERSION = "minimum_version"
    }

    override fun parse(value: String): PrinceOfVersionsConfig<String> {
        // Parse the integer from JSON, then convert it to a String for the config
        val mandatoryVersion = JSONObject(value).getInt(MINIMUM_VERSION).toString()
        return PrinceOfVersionsConfig(
            mandatoryVersion = mandatoryVersion,
            optionalVersion = null,
            optionalNotificationType = NotificationType.ALWAYS,
            metadata = emptyMap(),
            requirements = emptyMap()
        )
    }
}