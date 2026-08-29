package com.infinum.princeofversions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AppStoreResponseParserTest {

    @Test
    fun `parses valid response with one result`() {
        val json = """
            {
              "resultCount": 1,
              "results": [
                {
                  "version": "3.2.1",
                  "currentVersionReleaseDate": "2025-11-01T07:00:00Z"
                }
              ]
            }
        """.trimIndent()

        val info = AppStoreResponseParser.parse(json)
        assertEquals("3.2.1", info?.version)
        assertEquals("2025-11-01T07:00:00Z", info?.currentVersionReleaseDate)
    }

    @Test
    fun `returns null when resultCount is zero`() {
        val json = """{ "resultCount": 0, "results": [] }"""
        assertNull(AppStoreResponseParser.parse(json))
    }

    @Test
    fun `returns null when resultCount is missing`() {
        val json = """{ "results": [] }"""
        assertNull(AppStoreResponseParser.parse(json))
    }

    @Test
    fun `throws on malformed JSON`() {
        assertFailsWith<ConfigurationException> {
            AppStoreResponseParser.parse("not json")
        }
    }

    @Test
    fun `throws when response is not a JSON object`() {
        assertFailsWith<ConfigurationException> {
            AppStoreResponseParser.parse("""[ "array" ]""")
        }
    }

    @Test
    fun `throws when results key is missing`() {
        val json = """{ "resultCount": 1 }"""
        assertFailsWith<ConfigurationException> {
            AppStoreResponseParser.parse(json)
        }
    }

    @Test
    fun `throws when results array is empty despite resultCount being positive`() {
        val json = """{ "resultCount": 1, "results": [] }"""
        assertFailsWith<ConfigurationException> {
            AppStoreResponseParser.parse(json)
        }
    }

    @Test
    fun `throws when version field is missing`() {
        val json = """
            {
              "resultCount": 1,
              "results": [{ "currentVersionReleaseDate": "2025-11-01T07:00:00Z" }]
            }
        """.trimIndent()

        assertFailsWith<ConfigurationException> {
            AppStoreResponseParser.parse(json)
        }
    }

    @Test
    fun `throws when currentVersionReleaseDate is missing`() {
        val json = """
            {
              "resultCount": 1,
              "results": [{ "version": "3.2.1" }]
            }
        """.trimIndent()

        assertFailsWith<ConfigurationException> {
            AppStoreResponseParser.parse(json)
        }
    }

    @Test
    fun `ignores extra fields in response`() {
        val json = """
            {
              "resultCount": 1,
              "results": [
                {
                  "version": "1.0.0",
                  "currentVersionReleaseDate": "2025-01-01T00:00:00Z",
                  "trackName": "MyApp",
                  "bundleId": "com.example.app"
                }
              ]
            }
        """.trimIndent()

        val info = AppStoreResponseParser.parse(json)
        assertEquals("1.0.0", info?.version)
    }
}
