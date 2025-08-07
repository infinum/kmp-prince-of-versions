package com.infinum.princeofversions

@Suppress("UnusedPrivateProperty") // Remove once placeholder is fully implemented
internal class AndroidVersionComparator(
    private val externalComparator: VersionComparator<Int>
) : VersionComparator<Int> {
    override fun compare(firstVersion: Int, secondVersion: Int): Int {
        TODO("Not yet implemented")
    }
}
