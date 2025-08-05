package com.infinum.princeofversions

public actual suspend fun riskyCall(): String {
    delay(500)
    return "Hello from JVM!"
}
