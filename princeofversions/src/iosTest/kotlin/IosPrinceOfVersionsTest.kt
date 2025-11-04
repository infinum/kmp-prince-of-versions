package com.infinum.princeofversions

import com.infinum.princeofversions.PrinceOfVersionsBase.Companion.DEFAULT_NETWORK_TIMEOUT
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class PrinceOfVersionsIosTest {

    @Test
    fun `factory creates instance`() {
        val pov = PrinceOfVersions()
        assertNotNull(pov)
        assertIs<PrinceOfVersionsImpl>(pov)
    }

    @Test
    fun `propagates loader error`() = runTest {
        val pov = PrinceOfVersions()
        assertFailsWith<IoException> {
            pov.checkForUpdates(source = FakeLoaderError(IoException("boom")))
        }
    }

    @Test
    fun `invalid URL throws IoException via iOS loader`() = runTest {
        val pov = PrinceOfVersions()
        assertFailsWith<IoException> {
            pov.checkForUpdates(
                url = "not a url",
                username = null,
                password = null,
                networkTimeout = DEFAULT_NETWORK_TIMEOUT
            )
        }
    }

    @Test
    fun `both versions null throws IllegalStateException`() = runTest {
        val json = """{ "ios2": { "meta": { "note": "no versions present" } } }"""
        val pov = PrinceOfVersions()
        assertFailsWith<IllegalStateException> {
            pov.checkForUpdates(source = FakeLoaderSuccess(json))
        }
    }
}

/* -------- helpers -------- */

private class FakeLoaderSuccess(private val body: String) : Loader {
    override suspend fun load(): String = body
}

private class FakeLoaderError(private val t: Throwable) : Loader {
    override suspend fun load(): String = throw t
}
