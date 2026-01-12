package com.infinum.princeofversions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private class FakeInfoDictionaryProvider(
    private val map: Map<String, Any?>?
) : InfoDictionaryProvider {
    override fun infoDictionary(): Map<String, Any?>? = map
}

class IosApplicationVersionProviderTest {

    @Test
    fun `returns short-build when Info_plist has required keys`() {
        val sut = IosApplicationVersionProvider(
            infoProvider = FakeInfoDictionaryProvider(
                mapOf(
                    "CFBundleShortVersionString" to "1.2.3",
                    "CFBundleVersion" to "456"
                )
            )
        )
        assertEquals("1.2.3-456", sut.getVersion())
    }

    @Test
    fun `throws when Info_plist is missing`() {
        val sut = IosApplicationVersionProvider(
            infoProvider = FakeInfoDictionaryProvider(null)
        )
        assertFailsWith<IllegalStateException> { sut.getVersion() }
    }

    @Test
    fun `throws when CFBundleShortVersionString is missing or blank`() {
        // missing key
        val missingShort = IosApplicationVersionProvider(
            infoProvider = FakeInfoDictionaryProvider(
                mapOf("CFBundleVersion" to "456")
            )
        )
        assertFailsWith<IllegalStateException> { missingShort.getVersion() }

        // blank value
        val blankShort = IosApplicationVersionProvider(
            infoProvider = FakeInfoDictionaryProvider(
                mapOf(
                    "CFBundleShortVersionString" to "   ",
                    "CFBundleVersion" to "456"
                )
            )
        )
        assertFailsWith<IllegalStateException> { blankShort.getVersion() }
    }

    @Test
    fun `throws when CFBundleVersion is missing or blank`() {
        // missing key
        val missingBuild = IosApplicationVersionProvider(
            infoProvider = FakeInfoDictionaryProvider(
                mapOf("CFBundleShortVersionString" to "1.2.3")
            )
        )
        assertFailsWith<IllegalStateException> { missingBuild.getVersion() }

        // blank value
        val blankBuild = IosApplicationVersionProvider(
            infoProvider = FakeInfoDictionaryProvider(
                mapOf(
                    "CFBundleShortVersionString" to "1.2.3",
                    "CFBundleVersion" to ""
                )
            )
        )
        assertFailsWith<IllegalStateException> { blankBuild.getVersion() }
    }
}
