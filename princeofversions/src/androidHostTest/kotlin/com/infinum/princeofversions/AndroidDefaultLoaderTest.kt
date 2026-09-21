package com.infinum.princeofversions

import com.infinum.princeofversions.util.ResourceUtils
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class AndroidDefaultLoaderTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun cleanup() {
        mockWebServer.shutdown()
    }

    @Test
    fun `androidDefaultLoader should load content successfully when server returns valid response`() = runTest {
        val filename = "valid_update_full.json"
        val responseBody = ResourceUtils.readFromFile(filename)
        val response = MockResponse().setBody(responseBody).setResponseCode(200)
        mockWebServer.enqueue(response)

        val androidLoader = AndroidDefaultLoader(
            url = mockWebServer.url("/").toString(),
            username = null,
            password = null,
            networkTimeout = 60.seconds
        )
        val content = androidLoader.load()

        assertJsonEquals(content, responseBody)
    }

    @Test
    fun `androidDefaultLoader should throw IoException when network timeout is exceeded`() = runTest {
        val androidLoader = AndroidDefaultLoader(
            url = mockWebServer.url("/").toString(),
            username = null,
            password = null,
            networkTimeout = 1.seconds
        )
        assertFailsWith<IoException> {
            androidLoader.load()
        }
    }

    @Test
    fun `androidDefaultLoader should load malformed json content when server returns malformed response`() = runTest {
        val filename = "malformed_json.json"
        val responseBody = ResourceUtils.readFromFile(filename)
        val response = MockResponse().setBody(responseBody).setResponseCode(200)
        mockWebServer.enqueue(response)

        val androidLoader = AndroidDefaultLoader(
            url = mockWebServer.url("/").toString(),
            username = null,
            password = null,
            networkTimeout = 60.seconds
        )
        val content = androidLoader.load()

        assertJsonEquals(content, responseBody)
    }

    @Test
    fun `androidDefaultLoader should return empty string when server returns response without body`() = runTest {
        val response = MockResponse().setResponseCode(200) // No body
        mockWebServer.enqueue(response)

        val androidLoader = AndroidDefaultLoader(
            url = mockWebServer.url("/").toString(),
            username = null,
            password = null,
            networkTimeout = 60.seconds
        )
        val content = androidLoader.load()

        assertJsonEquals(content, "")
    }

    @Test
    fun `androidDefaultLoader should throw IoException when server returns error response`() = runTest {
        val response = MockResponse().setResponseCode(404)
        mockWebServer.enqueue(response)

        val androidLoader = AndroidDefaultLoader(
            url = mockWebServer.url("/").toString(),
            username = null,
            password = null,
            networkTimeout = 60.seconds
        )
        assertFailsWith<IoException> {
            androidLoader.load()
        }
    }

    @Test
    fun `androidDefaultLoader should load content and send authorization header when basic auth credentials are provided`() = runTest {
        val filename = "valid_update_full.json"
        val responseBody = ResourceUtils.readFromFile(filename)
        val response = MockResponse().setBody(responseBody).setResponseCode(200)
        mockWebServer.enqueue(response)

        val androidLoader = AndroidDefaultLoader(
            url = mockWebServer.url("/").toString(),
            username = "testuser",
            password = "testpass",
            networkTimeout = 60.seconds
        )
        val content = androidLoader.load()

        assertJsonEquals(content, responseBody)

        // Verify that the Authorization header was sent
        val request = mockWebServer.takeRequest()
        val authHeader = request.getHeader("Authorization")
        assert(authHeader?.startsWith("Basic ") == true)
    }

    @Test
    fun `androidDefaultLoader should throw IoException when server returns unauthorized response with wrong credentials`() = runTest {
        val response = MockResponse().setResponseCode(401)
        mockWebServer.enqueue(response)

        val androidLoader = AndroidDefaultLoader(
            url = mockWebServer.url("/").toString(),
            username = "wronguser",
            password = "wrongpass",
            networkTimeout = 60.seconds
        )
        assertFailsWith<IoException> {
            androidLoader.load()
        }
    }

    private fun assertJsonEquals(actual: String, expected: String) {
        assertEquals(actual.replace("\n", ""), expected.replace("\n", ""))
    }
}
