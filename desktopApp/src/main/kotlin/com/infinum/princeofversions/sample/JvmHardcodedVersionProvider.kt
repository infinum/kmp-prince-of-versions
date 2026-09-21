package com.infinum.princeofversions.sample

import com.infinum.princeofversions.ApplicationVersionProvider

/**
 * A JVM-specific version provider that returns a hardcoded version string.
 */
class JvmHardcodedVersionProvider : ApplicationVersionProvider {
    private val currentAppVersion = "26"
    override fun getVersion(): String {
        return currentAppVersion
    }
}