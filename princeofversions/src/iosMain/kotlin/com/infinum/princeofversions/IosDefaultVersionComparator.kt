package com.infinum.princeofversions

public typealias VersionComparator = BaseVersionComparator<String>

internal class IosDefaultVersionComparator : VersionComparator {

    private data class Parts(val major: Int, val minor: Int, val patch: Int, val build: Int)

    // Matches: "1", "1.2", "1.2.3", "1.2.3-45" (with optional surrounding whitespace)
    private val re = Regex("""^\s*(\d+)(?:\.(\d+))?(?:\.(\d+))?(?:-(\d+))?\s*$""")

    override fun compare(firstVersion: String, secondVersion: String): Int {
        val a = parse(firstVersion)
        val b = parse(secondVersion)
        return compareValuesBy(a, b, { it.major }, { it.minor }, { it.patch }, { it.build })
    }

    private fun parse(raw: String): Parts {
        val m = re.matchEntire(raw)
            ?: throw IllegalArgumentException("Invalid version string: '$raw'")

        val major = m.groupValues[GROUP_MAJOR].toInt()
        val minor = m.groupValues.getOrNull(GROUP_MINOR)?.takeIf { it.isNotEmpty() }?.toInt() ?: DEFAULT_NUM
        val patch = m.groupValues.getOrNull(GROUP_PATCH)?.takeIf { it.isNotEmpty() }?.toInt() ?: DEFAULT_NUM
        val build = m.groupValues.getOrNull(GROUP_BUILD)?.takeIf { it.isNotEmpty() }?.toInt() ?: DEFAULT_NUM

        return Parts(major, minor, patch, build)
    }

    companion object {
        private const val GROUP_MAJOR = 1
        private const val GROUP_MINOR = 2
        private const val GROUP_PATCH = 3
        private const val GROUP_BUILD = 4
        private const val DEFAULT_NUM = 0
    }
}
