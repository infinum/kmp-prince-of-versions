package com.infinum.princeofversions

import platform.Foundation.NSBundle

internal class MainBundleInfoDictionaryProvider(private val bundle: NSBundle = NSBundle.mainBundle) : InfoDictionaryProvider {

    override fun infoDictionary(): Map<String, Any?>? {
        val dict = bundle.infoDictionary ?: return null
        @Suppress("UNCHECKED_CAST")
        return dict as? Map<String, Any?>
    }
}
