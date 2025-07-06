package com.infinum.princeofversions

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform