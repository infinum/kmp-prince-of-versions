package com.infinum.princeofversions.util

import java.io.InputStream

/**
 * Utility methods for accessing resources bundled with test APK.
 * Resources should be placed under /resources folder in the jvmTest source set..
 */
object ResourceUtils {

    /**
     * Converts InputStream to String.
     */
    fun convertStreamToString(inputStream: InputStream): String {
        return inputStream.bufferedReader().use { it.readText() }
    }

    /**
     * Reads a resource file to String.
     */
    fun readFromFile(filename: String): String {
        val inputStream = ResourceUtils::class.java.classLoader?.getResourceAsStream("mockdata/$filename")
            ?: throw IllegalArgumentException("Resource file not found: $filename")
        return convertStreamToString(inputStream)
    }
}
