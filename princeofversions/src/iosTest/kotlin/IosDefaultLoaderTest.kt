@file:OptIn(kotlinx.cinterop.BetaInteropApi::class)

package com.infinum.princeofversions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLResponse
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.valueForHTTPHeaderField

class IosDefaultLoaderTest {

    @Test
    fun success_2xx_returns_null_failureMessage() {
        val http200 = httpResponse(200)
        val message = IosDefaultLoader_buildFailureMessage(
            error = null,
            response = http200
        )
        assertNull(message)
    }

    @Test
    fun non_http_response_returns_human_message() {
        val nonHttp = NSURLResponse() // not an NSHTTPURLResponse
        val msg = IosDefaultLoader_buildFailureMessage(
            error = null,
            response = nonHttp
        )
        assertTrue(msg!!.contains("non-HTTP", ignoreCase = true))
    }

    @Test
    fun http_non_2xx_returns_status_message() {
        val http404 = httpResponse(404)
        val msg = IosDefaultLoader_buildFailureMessage(
            error = null,
            response = http404
        )
        // Typical format: "HTTP 404: not found"
        assertTrue(msg!!.contains("HTTP 404"))
    }

    @Test
    fun nserror_is_reported_as_message() {
        val err = NSError.errorWithDomain("test.domain", code = 123, userInfo = null)
        val msg = IosDefaultLoader_buildFailureMessage(
            error = err,
            response = null
        )
        assertTrue(msg!!.isNotBlank())
    }

    @Test
    fun decodeBody_utf8_ok() {
        val body = """{"hello":"world"}"""
        val data = body.nsData()
        val decoded = IosDefaultLoader_decodeBody(data)
        assertEquals(body, decoded)
    }

    @Test
    fun decodeBody_null_or_empty_returns_empty_string() {
        assertEquals("", IosDefaultLoader_decodeBody(null))
        assertEquals("", IosDefaultLoader_decodeBody("".nsData()))
    }

    @Test
    fun createRequest_sets_custom_headers() {
        val loader = IosDefaultLoader(
            url = "https://example.com",
            username = null,
            password = null,
            networkTimeout = 60.seconds,
            headers = mapOf("x-api-key" to "secret-key", "X-Client" to "sample"),
        )
        val request = loader.createRequest(NSURL.URLWithString("https://example.com")!!)

        assertEquals("secret-key", request.valueForHTTPHeaderField("x-api-key"))
        assertEquals("sample", request.valueForHTTPHeaderField("X-Client"))
    }

    @Test
    fun createRequest_prefers_basic_auth_over_custom_authorization_header() {
        val loader = IosDefaultLoader(
            url = "https://example.com",
            username = "testuser",
            password = "testpass",
            networkTimeout = 60.seconds,
            headers = mapOf("Authorization" to "Bearer token"),
        )
        val request = loader.createRequest(NSURL.URLWithString("https://example.com")!!)

        assertEquals("Basic dGVzdHVzZXI6dGVzdHBhc3M=", request.valueForHTTPHeaderField("Authorization"))
    }

    // --- helpers ---

    private fun httpResponse(code: Int): NSHTTPURLResponse {
        val url = NSURL.URLWithString("https://example.com")!!
        // NSHTTPURLResponse(uRL:statusCode:HTTPVersion:headerFields:) is available to construct
        return NSHTTPURLResponse(
            uRL = url,
            statusCode = code.toLong(),
            HTTPVersion = null,
            headerFields = null
        )
    }

    private fun String.nsData(): NSData =
        NSString.create(string = this).dataUsingEncoding(NSUTF8StringEncoding)!!

    // Call through to the internal functions we exposed (simple wrappers for readability)
    private fun IosDefaultLoader_buildFailureMessage(
        error: NSError?,
        response: NSURLResponse?
    ): String? = com.infinum.princeofversions.IosDefaultLoader.buildFailureMessage(error, response)

    private fun IosDefaultLoader_decodeBody(
        data: NSData?
    ): String = com.infinum.princeofversions.IosDefaultLoader.decodeBody(data)
}
