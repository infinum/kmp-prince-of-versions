@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package com.infinum.princeofversions

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding

internal data class AppStoreVersionInfo(
    val version: String,
    val currentVersionReleaseDate: String,
)

internal object AppStoreResponseParser {

    /**
     * Parses the iTunes Lookup API response JSON.
     * Returns null if no results found (resultCount == 0 or results array empty).
     * Throws [ConfigurationException] on malformed JSON.
     */
    fun parse(jsonString: String): AppStoreVersionInfo? {
        val json = deserialize(jsonString)

        val resultCount = (json["resultCount"] as? Number)?.toInt() ?: 0
        if (resultCount == 0) return null

        val firstResult = extractFirstResult(json)

        val version = firstResult["version"] as? String
            ?: configurationError("App Store result missing 'version' field")

        val releaseDate = firstResult["currentVersionReleaseDate"] as? String
            ?: configurationError("App Store result missing 'currentVersionReleaseDate' field")

        return AppStoreVersionInfo(
            version = version,
            currentVersionReleaseDate = releaseDate,
        )
    }

    private fun deserialize(jsonString: String): Map<*, *> {
        val data = NSString.create(string = jsonString).dataUsingEncoding(NSUTF8StringEncoding)
            ?: configurationError("Failed to encode App Store response as UTF-8")

        val parsed = try {
            NSJSONSerialization.JSONObjectWithData(data, options = 0u, error = null)
        } catch (e: Exception) {
            throw ConfigurationException("Failed to parse App Store JSON response", e)
        }

        return parsed as? Map<*, *>
            ?: configurationError("App Store response is not a JSON object")
    }

    private fun extractFirstResult(json: Map<*, *>): Map<*, *> {
        val results = json["results"] as? List<*>
            ?: configurationError("App Store response missing 'results' array")

        return results.firstOrNull() as? Map<*, *>
            ?: configurationError("App Store response 'results' array is empty or malformed")
    }

    private fun configurationError(message: String): Nothing {
        throw ConfigurationException(message)
    }
}
