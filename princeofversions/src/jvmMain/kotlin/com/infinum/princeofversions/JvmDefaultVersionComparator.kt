package com.infinum.princeofversions

public typealias VersionComparator = BaseVersionComparator<String>

/**
 * The default version comparator for JVM, which compares simple dotted numeric versions.
 * This implementation handles versions consisting of one or more non-negative integers
 * separated by dots (e.g., "1.2.3").
 *
 * For more complex use cases, such as comparing versions with pre-release tags (e.g., "-beta"),
 * you can provide a custom [VersionComparator] implementation. A library like
 * 'jsemver' can be used for full Semantic Versioning 2.0.0 support.
 */
internal class JvmDefaultVersionComparator : VersionComparator {

    /**
     * Compares two simple dotted numeric version strings.
     *
     * @param firstVersion The first version string (e.g., "1.2.3").
     * @param secondVersion The second version string (e.g., "1.10.0").
     *
     * @return Zero if the versions are equal, a positive number if firstVersion is greater,
     * or a negative number if firstVersion is smaller.
     * @throws IllegalArgumentException if either version string is malformed.
     */
    override fun compare(firstVersion: String, secondVersion: String): Int {
        val firstParts = parseVersion(firstVersion)
        val secondParts = parseVersion(secondVersion)

        val maxParts = maxOf(firstParts.size, secondParts.size)

        for (i in 0 until maxParts) {
            val firstPart = firstParts.getOrElse(i) { 0L }
            val secondPart = secondParts.getOrElse(i) { 0L }

            if (firstPart != secondPart) {
                return firstPart.compareTo(secondPart)
            }
        }
        return 0
    }

    /**
     * Parses and validates a version string.
     *
     * @param version The version string to parse.
     * @return A list of version components as Longs.
     * @throws IllegalArgumentException if the version string is invalid.
     */
    private fun parseVersion(version: String): List<Long> {
        val trimmedVersion = version.trim()
        require(trimmedVersion.isNotBlank()) { "Version string cannot be blank." }

        return trimmedVersion.split('.').map { part ->
            require(part.isNotEmpty()) {
                "Version string cannot contain empty parts (e.g., '1..2'). Invalid version: '$version'"
            }
            part.toLongOrNull()
                ?: throw IllegalArgumentException(
                    "Version part '$part' is not numeric in version: '$version'",
                )
        }
    }
}
