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
class JvmPrinceOfVersionsTest {

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
    fun `princeOfVersions should create instance successfully when builder block without mainClass is provided`() {
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
    fun `princeOfVersions should throw exception when storage is missing in builder without mainClass`() {
        assertFailsWith<IllegalArgumentException> {
            PrinceOfVersions {
                // Missing storage - should fail
                withVersionProvider(TestVersionProvider())
            }
        }
    }

    @Test
    fun `princeOfVersions should create instance successfully when mainClass is provided`() {
        val mainClass = JvmPrinceOfVersionsTest::class.java

        val princeOfVersions = PrinceOfVersions(mainClass)

        assertNotNull(princeOfVersions)
        assertTrue(princeOfVersions is PrinceOfVersionsImpl)
    }

    @Test
    fun `princeOfVersions should create instance successfully when mainClass and builder block are provided`() {
        val mainClass = JvmPrinceOfVersionsTest::class.java

        val princeOfVersions = PrinceOfVersions(mainClass) {
            withVersionProvider(TestVersionProvider())
            withConfigurationParser(TestConfigurationParser())
            withVersionComparator(TestVersionComparator())
        }

        assertNotNull(princeOfVersions)
        assertTrue(princeOfVersions is PrinceOfVersionsImpl)
    }

    @Test
    fun `checkForUpdatesFromUrl should successfully check for updates when server returns valid response`() = runTest {
        val components = createTestComponents()
        val princeOfVersions = PrinceOfVersions(components)
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""{"optional_version": "2.0.0", "notification_type": "ONCE"}""")
        mockWebServer.enqueue(mockResponse)

        val result = princeOfVersions.checkForUpdatesFromUrl(mockWebServer.url("/").toString())

        assertNotNull(result)
    }

    @Test
    fun `checkForUpdatesFromUrl should throw IOException when server returns error response`() = runTest {
        val components = createTestComponents()
        val princeOfVersions = PrinceOfVersions(components)
        val mockResponse = MockResponse().setResponseCode(404)
        mockWebServer.enqueue(mockResponse)

        assertFailsWith<java.io.IOException> {
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
    private var lastVersion: String? = null

    override suspend fun getLastSavedVersion(): String? = lastVersion
    override suspend fun saveVersion(version: String) {
        lastVersion = version
    }
}

private class TestVersionProvider : ApplicationVersionProvider {
    override fun getVersion(): String = "1.0.0"
}

private class TestConfigurationParser : ConfigurationParser {
    override fun parse(value: String): PrinceOfVersionsConfig {
        return PrinceOfVersionsConfig(
            mandatoryVersion = null,
            optionalVersion = "2.0.0",
            optionalNotificationType = NotificationType.ONCE,
            metadata = mapOf("title" to "Test Update", "description" to "Test Description"),
            requirements = emptyMap()
        )
    }
}

private class TestVersionComparator : VersionComparator {
    override fun compare(firstVersion: String, secondVersion: String): Int = firstVersion.compareTo(secondVersion)
}
