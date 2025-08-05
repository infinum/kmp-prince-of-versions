package com.infinum.princeofversions

public actual suspend fun riskyCall(): String {
    throw IllegalStateException("This is an iOS-specific error from Kotlin/Native")
}
