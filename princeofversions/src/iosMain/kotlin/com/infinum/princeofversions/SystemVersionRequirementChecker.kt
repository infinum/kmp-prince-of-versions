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

    private fun parseVersion(input: String): Version =
        VersionParser.parseDots(input) // reuse shared parser

    companion object {
        const val KEY = "required_os_version"
    }
}

public fun makeSystemVersionRequirementChecker(): RequirementChecker =
    SystemVersionRequirementChecker()
