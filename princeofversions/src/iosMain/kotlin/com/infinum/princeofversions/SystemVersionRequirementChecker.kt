package com.infinum.princeofversions

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo

/**
 * Checks the device's iOS version against a required minimum (e.g., "12.1.2").
 * Compares (major, minor, patch) numerically.
 */
internal class SystemVersionRequirementChecker : RequirementChecker {

    override fun checkRequirements(value: String?): Boolean {
        val required = value?.let(::parse) ?: return false
        val current = currentOs()
        return compareTriples(current, required) >= 0 // current >= required
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun currentOs(): Triple<Int, Int, Int> {
        // NSProcessInfo.processInfo.operatingSystemVersion is a C struct; use useContents to read fields.
        val os = NSProcessInfo.processInfo.operatingSystemVersion
        return os.useContents {
            Triple(majorVersion.toInt(), minorVersion.toInt(), patchVersion.toInt())
        }
    }

    private fun parse(s: String): Triple<Int, Int, Int> {
        val parts = s.split('.')
        val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
        return Triple(major, minor, patch)
    }

    private fun compareTriples(a: Triple<Int, Int, Int>, b: Triple<Int, Int, Int>): Int {
        if (a.first != b.first) return a.first - b.first
        if (a.second != b.second) return a.second - b.second
        return a.third - b.third
    }

    companion object {
        const val KEY = "required_os_version"
    }
}
