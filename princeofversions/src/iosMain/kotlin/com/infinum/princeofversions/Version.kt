package com.infinum.princeofversions

/** Simple semantic version holder for (major.minor.patch). */
internal data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
) : Comparable<Version> {
    override fun compareTo(other: Version): Int =
        compareValuesBy(this, other, Version::major, Version::minor, Version::patch)
}
