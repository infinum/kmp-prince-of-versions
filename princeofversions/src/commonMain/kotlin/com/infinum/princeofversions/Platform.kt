package com.infinum.princeofversions

public interface Platform {
    public val name: String
}

public expect fun getPlatform(): Platform
