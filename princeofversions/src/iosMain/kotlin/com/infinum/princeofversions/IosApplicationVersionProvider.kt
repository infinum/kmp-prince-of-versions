package com.infinum.princeofversions

import platform.Foundation.NSBundle

internal interface IosInfoPlist {
    fun string(key: String): String?
}

internal class RealIosInfoPlist(
    private val bundle: NSBundle = NSBundle.mainBundle(),
) : IosInfoPlist {
    override fun string(key: String): String? =
        bundle.objectForInfoDictionaryKey(key) as? String
}

public typealias ApplicationVersionProvider = BaseApplicationVersionProvider<String>

internal class IosApplicationVersionProvider(
    private val infoProvider: InfoDictionaryProvider = MainBundleInfoDictionaryProvider(),
) : ApplicationVersionProvider {

    override fun getVersion(): String {
        val info = infoProvider.infoDictionary()
            ?: error("Info.plist not loaded (NSBundle.mainBundle.infoDictionary == null).")
        val short = (info["CFBundleShortVersionString"] as? String)?.takeIf { it.isNotBlank() }
            ?: error("CFBundleShortVersionString is missing or blank in Info.plist.")
        val build = (info["CFBundleVersion"] as? String)?.takeIf { it.isNotBlank() }
            ?: error("CFBundleVersion is missing or blank in Info.plist.")
        return "$short-$build"
    }
}

internal class HardcodedVersionProviderIos(
    private val current: String = "1.2.3",
) : ApplicationVersionProvider {
    override fun getVersion(): String = current
}
