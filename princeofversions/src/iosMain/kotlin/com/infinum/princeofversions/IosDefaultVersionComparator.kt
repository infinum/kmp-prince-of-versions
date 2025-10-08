package com.infinum.princeofversions

public typealias VersionComparator = BaseVersionComparator<String>

internal class IosDefaultVersionComparator : VersionComparator {
    override fun compare(firstVersion: String, secondVersion: String): Int {
        val a = parse(firstVersion)
        val b = parse(secondVersion)
        return compareValuesBy(a, b, { it.major }, { it.minor }, { it.patch }, { it.build })
    }

    private data class Parts(val major: Int, val minor: Int, val patch: Int, val build: Int)

    private fun parse(raw: String): Parts {
        // Split into "version" and optional "build" by the first '-'
        val (versionPart, buildPart) = raw.split("-", limit = 2).let { parts ->
            parts[0] to parts.getOrNull(1)
        }

        // versionPart -> "major[.minor[.patch]]"
        val comps = if (versionPart.isEmpty()) emptyList() else versionPart.split('.')

        require(comps.isNotEmpty()) { "Invalid version string: '$raw'" }

        val major = comps.getOrNull(0)?.toIntOrNull()
            ?: throw IllegalArgumentException("Invalid major version in '$raw'")

        val minor = comps.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = comps.getOrNull(2)?.toIntOrNull() ?: 0
        val build = buildPart?.trim()?.toIntOrNull() ?: 0

        return Parts(major, minor, patch, build)
    }
}
