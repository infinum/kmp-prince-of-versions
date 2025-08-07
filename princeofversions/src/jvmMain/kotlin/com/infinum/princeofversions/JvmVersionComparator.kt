package com.infinum.princeofversions

@Suppress("UnusedPrivateProperty") // Remove once placeholder is fully implemented
internal class JvmVersionComparator(
    private val externalComparator: VersionComparator<String>
) : VersionComparator<String> {
    override fun compare(firstVersion: String, secondVersion: String): Int {
        TODO("Not yet implemented")
    }
}
