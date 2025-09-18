package com.infinum.princeofversions

public typealias VersionComparator = BaseVersionComparator<String>

internal class IosDefaultVersionComparator : VersionComparator {
    @Suppress("NotImplementedDeclaration")
    override fun compare(firstVersion: String, secondVersion: String): Int {
        TODO("Not yet implemented")
    }
}
