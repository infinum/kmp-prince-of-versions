package com.infinum.princeofversions

public typealias VersionComparator = BaseVersionComparator<Int>

internal class AndroidDefaultVersionComparator : VersionComparator {
    override fun compare(firstVersion: Int, secondVersion: Int): Int {
        TODO("Not yet implemented")
    }
}
