package com.infinum.princeofversions

import platform.Foundation.NSBundle

public typealias ApplicationVersionProvider = BaseApplicationVersionProvider<String>

internal class IosApplicationVersionProvider : ApplicationVersionProvider {

    override fun getVersion(): String {
        val dictionary = NSBundle.mainBundle.infoDictionary
        val short = dictionary?.get("CFBundleShortVersionString") as? String ?: "0.0.0"
        val build = dictionary?.get("CFBundleVersion") as? String ?: "0"
        return "$short-$build"
    }
}

public class HardcodedVersionProviderIos(
    private val current: String = "1.2.3",
) : ApplicationVersionProvider {
    override fun getVersion(): String = current
}
