package com.infinum.princeofversions

public fun interface VersionComparator<T> {
    /**
     * Compares two versions and returns an integer value.
     *
     * @param firstVersion the first version to compare
     * @param secondVersion the second version to compare
     *
     * @return Zero if the values is equal, a positive number if firstVersion is greater than secondVersion,
     * or a negative number if firstVersion is less than secondVersion.
     */
    public fun compare(firstVersion: T, secondVersion: T): Int
}

internal fun <T> VersionComparator<T>.isVersionGreaterThan(firstVersion: T, secondVersion: T): Boolean {
    return compare(firstVersion, secondVersion) > 0
}
