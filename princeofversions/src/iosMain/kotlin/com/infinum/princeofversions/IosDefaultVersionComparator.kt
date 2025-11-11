package com.infinum.princeofversions

public typealias VersionComparator = BaseVersionComparator<String>

internal class IosDefaultVersionComparator : VersionComparator {

    private data class Parts(val major: Int, val minor: Int, val patch: Int, val build: Int)

    // Matches: "1", "1.2", "1.2.3", "1.2.3-45" (with optional surrounding whitespace)
    private val regex = Regex("""^\s*(\d+)(?:\.(\d+))?(?:\.(\d+))?(?:-(\d+))?\s*$""")

    override fun compare(firstVersion: String, secondVersion: String): Int {
        val parsedFirstVersion = parse(firstVersion)
        val parsedSecondVersion = parse(secondVersion)
        return compareValuesBy(
            a = parsedFirstVersion,
            b = parsedSecondVersion,
            selectors = arrayOf(
                { it.major },
                { it.minor },
                { it.patch },
                { it.build },
            ),
        )
    }

    private fun parse(raw: String): Parts {
        val matches = regex.matchEntire(raw)
            ?: throw IllegalArgumentException("Invalid version string: '$raw'")

        val major = matches.groupValues[GROUP_MAJOR].toInt()
        val minor = matches.groupValues.getOrNull(GROUP_MINOR)?.takeIf { it.isNotEmpty() }?.toInt() ?: DEFAULT_NUM
        val patch = matches.groupValues.getOrNull(GROUP_PATCH)?.takeIf { it.isNotEmpty() }?.toInt() ?: DEFAULT_NUM
        val build = matches.groupValues.getOrNull(GROUP_BUILD)?.takeIf { it.isNotEmpty() }?.toInt() ?: DEFAULT_NUM

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

public fun defaultIosVersionComparator(): VersionComparator = IosDefaultVersionComparator()

public class DevBuildVersionComparator(
    private val delegate: VersionComparator = IosDefaultVersionComparator(),
) : VersionComparator {

    override fun compare(firstVersion: String, secondVersion: String): Int {
        val remote = secondVersion?.trim() ?: ""
        // Treat any remote ending with "-0" as a dev build => no update
        if (remote.endsWith("-0")) {
            // Return 0 (equal) OR positive (current > remote). Either suppresses an update.
            return 0
        }
        return delegate.compare(firstVersion, secondVersion)
    }
}

public fun princeOfVersionsWithCustomVersionLogic(
    provider: ApplicationVersionProvider,
    comparator: VersionComparator,
): PrinceOfVersions {
    val components = PrinceOfVersionsComponents.Builder()
        .withVersionProvider(provider)
        .withVersionComparator(comparator)
        .build()
    return createPrinceOfVersions(components)
}
