package com.infinum.princeofversions

internal data class VersionWithBuild(
    val version: Version,
    val build: Int,
)

internal object VersionParser {

    // Matches: "1", "1.2", "1.2.3", "1.2.3-45" (+ optional whitespace)
    private val regex = Regex("""^\s*(\d+)(?:\.(\d+))?(?:\.(\d+))?(?:-(\d+))?\s*$""")

    /**
     * Strict parser for "1", "1.2", "1.2.3", "1.2.3-45".
     * Throws on invalid input.
     */
    fun parseWithBuild(raw: String): VersionWithBuild {
        val match = regex.matchEntire(raw)
            ?: throw IllegalArgumentException("Invalid version string: '$raw'")

        fun groupInt(idx: Int): Int =
            match.groupValues.getOrNull(idx)
                ?.takeIf { it.isNotEmpty() }
                ?.toInt()
                ?: 0

        val major = groupInt(1)
        val minor = groupInt(2)
        val patch = groupInt(3)
        val build = groupInt(4)

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