package com.infinum.princeofversions

internal data class VersionWithBuild(
    val version: Version,
    val build: Int,
)

internal object VersionParser {

    // Matches: "1", "1.2", "1.2.3", "1.2.3-45" (+ optional whitespace)
    private val regex = Regex("""^\s*(\d+)(?:\.(\d+))?(?:\.(\d+))?(?:-(\d+))?\s*$""")

    private const val MAJOR = 1
    private const val MINOR = 2
    private const val PATCH = 3
    private const val BUILD = 4

    /**
     * Strictly parses version strings like "1", "1.2", "1.2.3" and "1.2.3-45".
     * Missing version segments default to 0, so "1-45" becomes "1.0.0-45".
     * Throws [IllegalArgumentException] on invalid input.
     */
    fun parseWithBuild(raw: String): VersionWithBuild {
        val match = regex.matchEntire(raw)
            ?: throw IllegalArgumentException("Invalid version string: '$raw'")

        fun groupInt(idx: Int): Int =
            match.groupValues.getOrNull(idx)
                ?.takeIf { it.isNotEmpty() }
                ?.toInt()
                ?: 0

        val major = groupInt(MAJOR)
        val minor = groupInt(MINOR)
        val patch = groupInt(PATCH)
        val build = groupInt(BUILD)

        return VersionWithBuild(
            version = Version(major, minor, patch),
            build = build,
        )
    }

    /**
     * Tolerant parser for "1", "1.2", "1.2.3" (ignores any "-build" suffix).
     */
    fun parseDots(raw: String): Version {
        val cleaned = raw.substringBefore('-')
        val parts = cleaned.split('.')

        fun partOrZero(idx: Int) =
            parts.getOrNull(idx)?.toIntOrNull() ?: 0

        return Version(
            major = partOrZero(0),
            minor = partOrZero(1),
            patch = partOrZero(2),
        )
    }
}
