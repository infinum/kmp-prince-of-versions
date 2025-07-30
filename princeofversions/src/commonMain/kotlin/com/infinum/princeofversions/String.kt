package com.infinum.princeofversions

private const val VERSION_REGEX = "\\d+(\\.\\d+)*"

internal fun String.isVersionGreater(other: String): Boolean {
    var matches = false

    this.verifyVersionFormat()
    other.verifyVersionFormat()

    val thisParts = this.split(".")
    val otherParts = other.split(".")

    for (i in 0 until maxOf(thisParts.size, otherParts.size)) {
        val thisPart = thisParts.getOrNull(i)?.toIntOrNull() ?: 0
        val otherPart = otherParts.getOrNull(i)?.toIntOrNull() ?: 0

        if (thisPart > otherPart) matches = true
    }

    return matches
}

internal fun String.verifyVersionFormat() {
    if (!Regex(VERSION_REGEX).matches(this)) {
        throw IllegalArgumentException(
            "Version format is invalid: '$this'. " +
                "Expected format is 'major.minor.patch' or similar, e.g. '1.0.0'."
        )
    }
}
