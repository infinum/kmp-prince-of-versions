package com.infinum.princeofversions

/**
 * Compares two versions and returns an integer value.
 *
 * Implemented method should return zero if the values is equal, a positive number if firstVersion is greater than secondVersion,
 * or a negative number if firstVersion is less than secondVersion.
 */
public typealias VersionComparator = BaseVersionComparator<Long>

/**
 * The default version comparator for Android, which compares two integer versions.
 */
internal class AndroidDefaultVersionComparator : VersionComparator {
    /**
     * Compares two versions and returns an integer value.
     *
     * @param firstVersion the first version to compare
     * @param secondVersion the second version to compare
     *
     * @return Zero if the values is equal, a positive number if firstVersion is greater than secondVersion,
     * or a negative number if firstVersion is less than secondVersion.
     */
    override fun compare(firstVersion: Long, secondVersion: Long): Int = firstVersion.compareTo(secondVersion)
}
