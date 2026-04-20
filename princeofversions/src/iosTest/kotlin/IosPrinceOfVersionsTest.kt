package com.infinum.princeofversions

import com.infinum.princeofversions.PrinceOfVersionsBase.Companion.DEFAULT_NETWORK_TIMEOUT
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class PrinceOfVersionsIosTest {

    @Test
    fun `factory creates instance`() {
        val pov = testPrinceOfVersionsWithInfo()
        assertNotNull(pov)
        assertIs<PrinceOfVersionsImpl>(pov)
    }

    @Test
    fun `propagates loader error`() = runTest {
        val pov = testPrinceOfVersionsWithInfo()
        assertFailsWith<IoException> { pov.checkForUpdates(source = FakeLoaderError(IoException("boom"))) }
    }

    @Test
    fun `invalid URL throws IoException via iOS loader`() = runTest {
        val pov = testPrinceOfVersionsWithInfo()
        assertFailsWith<IoException> {
            pov.checkForUpdatesFromUrl(
                url = "not a url",
                username = null,
                password = null,
                networkTimeout = DEFAULT_NETWORK_TIMEOUT,
            )
        }
    }

    @Test
    fun `both versions null throws IllegalStateException`() = runTest {
        val pov = testPrinceOfVersionsWithInfo()
        val json = """{ "ios2": { "meta": { "note": "no versions present" } } }"""
        assertFailsWith<IllegalStateException> { pov.checkForUpdates(source = FakeLoaderSuccess(json)) }
    }

    internal fun testPrinceOfVersionsWithInfo(
        short: String = "1.0.0",
        buildNum: String = "1"
    ): PrinceOfVersions {
        val fakeInfo = object : InfoDictionaryProvider {
            override fun infoDictionary(): Map<String, Any?> = mapOf(
                "CFBundleShortVersionString" to short,
                "CFBundleVersion" to buildNum
            )
        }

        val appVersionProvider: ApplicationVersionProvider = IosApplicationVersionProvider(fakeInfo)

        val components = PrinceOfVersionsComponents
            .Builder()
            .withVersionProvider(appVersionProvider)
            .build()

        return createPrinceOfVersions(components)
    }
}


/* -------- helpers -------- */

private class FakeLoaderSuccess(private val body: String) : Loader {
    override suspend fun load(): String = body
}

private class FakeLoaderError(private val t: Throwable) : Loader {
    override suspend fun load(): String = throw t
}
