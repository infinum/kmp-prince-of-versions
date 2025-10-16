package com.infinum.princeofversions

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo

/**
 * Checks the device's iOS version against a required minimum (e.g., "12.1.2").
 * Compares versions numerically as (major, minor, patch).
 */
internal class SystemVersionRequirementChecker : RequirementChecker {

    override fun checkRequirements(value: String?): Boolean {
        val required = value?.let(::parseVersion) ?: return false
        val current = currentOs()
        return current >= required
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun currentOs(): Version {
        val os = NSProcessInfo.processInfo.operatingSystemVersion
        return os.useContents {
            Version(
                major = majorVersion.toInt(),
                minor = minorVersion.toInt(),
                patch = patchVersion.toInt(),
            )
        }
    }

    private fun parseVersion(input: String): Version {
        val components = input.split(SEP_DOT)
        return Version(
            major = components.getOrElse(INDEX_MAJOR) { DEFAULT_COMPONENT }.toIntOrNull() ?: DEFAULT_COMPONENT,
            minor = components.getOrElse(INDEX_MINOR) { DEFAULT_COMPONENT }.toIntOrNull() ?: DEFAULT_COMPONENT,
            patch = components.getOrElse(INDEX_PATCH) { DEFAULT_COMPONENT }.toIntOrNull() ?: DEFAULT_COMPONENT,
        )
    }

    companion object {
        const val KEY = "required_os_version"

        // Parsing helpers
        private const val SEP_DOT = '.'
        private const val INDEX_MAJOR = 0
        private const val INDEX_MINOR = 1
        private const val INDEX_PATCH = 2
        private const val DEFAULT_COMPONENT = "0"
    }
}

/** Simple semantic version holder for (major.minor.patch). */
private data class Version(
    val major: Comparable<Nothing>,
    val minor: Comparable<Nothing>,
    val patch: Comparable<Nothing>,
) : Comparable<Version> {
    override fun compareTo(other: Version): Int =
        compareValuesBy(this, other, Version::major, Version::minor, Version::patch)
}
