package com.infinum.princeofversions.sample

import com.infinum.princeofversions.VersionComparator

/**
 * A JVM-specific comparator with a special rule for developer builds.
 */
class JvmDeveloperBuildVersionComparator : VersionComparator<String> {
    /**
     * Compares versions, but treats any remote version ending in '0' as a
     * developer build that should not trigger an update.
     */
    override fun compare(firstVersion: String, secondVersion: String): Int {
        val firstInt = firstVersion.toIntOrNull() ?: -1
        val secondInt = secondVersion.toIntOrNull() ?: -1

        // Custom rule: never show an update for developer builds (versions ending in 0)
        if (secondInt != -1 && secondInt % 10 == 0) {
            return -1 // Treat as "no update available"
        }
        return secondInt.compareTo(firstInt)
    }
}