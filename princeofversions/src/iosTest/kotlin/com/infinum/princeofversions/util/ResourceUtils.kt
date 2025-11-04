package com.infinum.princeofversions.util

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL

/**
 * Utility methods for accessing resources bundled with the iOS app/test target.
 * Place files under a "mockdata" folder in the target and ensure Target Membership is checked.
 */
object ResourceUtils {

    /**
     * Reads a resource file from the app/test bundle (under "mockdata/") into a String.
     *
     * Example: ResourceUtils.readFromFile("config.json")
     */
    fun readFromFile(filename: String): String {
        val (name, ext) = splitNameExt(filename)

        // Try "mockdata/<filename>" first, then bundle root
        val url = NSBundle.mainBundle.URLForResource(name, ext, subdirectory = "mockdata")
            ?: NSBundle.mainBundle.URLForResource(name, ext, null)
            ?: throw IllegalArgumentException("Resource file not found: $filename")

        val data: NSData = NSData.dataWithContentsOfURL(url)
            ?: throw IllegalArgumentException("Unable to read data for: $filename")

        return data.toUtf8String()
            ?: throw IllegalArgumentException("Unable to decode UTF-8 for: $filename")
    }

    // -------- Helpers --------

    @OptIn(BetaInteropApi::class)
    private fun NSData.toUtf8String(): String? =
        NSString.create(this, NSUTF8StringEncoding)?.toString()

    private fun splitNameExt(file: String): Pair<String, String?> {
        val index = file.lastIndexOf('.')
        return if (index in 1 until file.length - 1) {
            file.substring(0, index) to file.substring(index + 1)
        } else {
            file to null
        }
    }
}
