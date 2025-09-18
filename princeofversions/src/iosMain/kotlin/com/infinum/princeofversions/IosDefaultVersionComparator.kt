package com.infinum.princeofversions

public typealias VersionComparator = BaseVersionComparator<String>

internal class IosDefaultVersionComparator : VersionComparator {
    override fun compare(firstVersion: String, secondVersion: String): Int {
        TODO("Not yet implemented")
    }
}
