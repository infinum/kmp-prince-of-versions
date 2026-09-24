package com.infinum.princeofversions

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(JUnit4::class)
class AndroidPrinceOfVersionsTest {

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
    fun `princeOfVersions should create instance successfully when custom components are provided`() {
        val components = createTestComponents()

        val princeOfVersions = PrinceOfVersions(components)

        assertNotNull(princeOfVersions)
        assertTrue(princeOfVersions is PrinceOfVersionsImpl)
    }

    @Test
    fun `princeOfVersions should create instance successfully when builder block without context is provided`() {
        val princeOfVersions = PrinceOfVersions {
            withStorage(TestStorage())
            withVersionProvider(TestVersionProvider())
            withConfigurationParser(TestConfigurationParser())
            withVersionComparator(TestVersionComparator())
        }

        assertNotNull(princeOfVersions)
        assertTrue(princeOfVersions is PrinceOfVersionsImpl)
    }

    @Test
    fun `princeOfVersions should throw exception when storage is missing in builder without context`() {
        assertFailsWith<IllegalArgumentException> {
            PrinceOfVersions {
                // Missing storage - should fail
                withVersionProvider(TestVersionProvider())
            }
        }
    }

    @Test
    fun `princeOfVersions should throw exception when versionProvider is missing in builder without context`() {
        assertFailsWith<IllegalArgumentException> {
            PrinceOfVersions {
                withStorage(TestStorage())
                // Missing versionProvider - should fail
            }
        }
    }

    @Test
    fun `checkForUpdatesFromUrl should successfully check for updates when server returns valid response`() = runTest {
        val components = createTestComponents()
        val princeOfVersions = PrinceOfVersions(components)
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""{"optional_version": 200, "notification_type": "ONCE"}""")
        mockWebServer.enqueue(mockResponse)

        val result = princeOfVersions.checkForUpdatesFromUrl(mockWebServer.url("/").toString())

        assertNotNull(result)
    }

    @Test
    fun `checkForUpdatesFromUrl should throw IoException when server returns error response`() = runTest {
        val components = createTestComponents()
        val princeOfVersions = PrinceOfVersions(components)
        val mockResponse = MockResponse().setResponseCode(404)
        mockWebServer.enqueue(mockResponse)

        assertFailsWith<IoException> {
            princeOfVersions.checkForUpdatesFromUrl(mockWebServer.url("/").toString())
        }
    }

    private fun createTestComponents(): PrinceOfVersionsComponents {
        return PrinceOfVersionsComponents.Builder()
            .withStorage(TestStorage())
            .withVersionProvider(TestVersionProvider())
            .withConfigurationParser(TestConfigurationParser())
            .withVersionComparator(TestVersionComparator())
            .build()
    }
}

private class TestStorage : Storage {
    private var lastVersion: Long? = null

    override suspend fun getLastSavedVersion(): Long? = lastVersion
    override suspend fun saveVersion(version: Long) {
        lastVersion = version
    }
}

private class TestVersionProvider : ApplicationVersionProvider {
    override fun getVersion(): Long = 100L
}

private class TestConfigurationParser : ConfigurationParser {
    override fun parse(value: String): BasePrinceOfVersionsConfig<Long> {
        return BasePrinceOfVersionsConfig(
            mandatoryVersion = null,
            optionalVersion = 200L,
            optionalNotificationType = NotificationType.ONCE,
            metadata = mapOf("title" to "Test Update", "description" to "Test Description"),
            requirements = emptyMap()
        )
    }
}

private class TestVersionComparator : VersionComparator {
    override fun compare(firstVersion: Long, secondVersion: Long): Int = firstVersion.compareTo(secondVersion)
}
