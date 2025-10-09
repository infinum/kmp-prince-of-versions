package com.infinum.princeofversions

public typealias VersionComparator = BaseVersionComparator<String>

internal class IosDefaultVersionComparator : VersionComparator {

    override fun compare(firstVersion: String, secondVersion: String): Int {
        val a = parse(firstVersion)
        val b = parse(secondVersion)
        return compareValuesBy(a, b, { it.major }, { it.minor }, { it.patch }, { it.build })
    }

    private data class Parts(val major: Int, val minor: Int, val patch: Int, val build: Int)

    // Matches: "1", "1.2", "1.2.3", "1.2.3-45" (with optional surrounding whitespace)
    private val re = Regex("""^\s*(\d+)(?:\.(\d+))?(?:\.(\d+))?(?:-(\d+))?\s*$""")

    private fun parse(raw: String): Parts {
        val m = re.matchEntire(raw)
            ?: throw IllegalArgumentException("Invalid version string: '$raw'")

        val major = m.groupValues[1].toInt()
        val minor = m.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }?.toInt() ?: 0
        val patch = m.groupValues.getOrNull(3)?.takeIf { it.isNotEmpty() }?.toInt() ?: 0
        val build = m.groupValues.getOrNull(4)?.takeIf { it.isNotEmpty() }?.toInt() ?: 0

        return Parts(major, minor, patch, build)
    }
}
