package com.infinum.princeofversions.models

internal interface Storage {
    fun getLastSavedVersion(): String?

    fun saveVersion(version: String)
}
