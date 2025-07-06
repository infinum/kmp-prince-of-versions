package com.infinum.princeofversions

private class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

public actual fun getPlatform(): Platform = JVMPlatform()
