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
        val data = NSString.create(string = jsonString).dataUsingEncoding(NSUTF8StringEncoding)
            ?: throw ConfigurationException("Failed to encode App Store response as UTF-8")

        val json = try {
            NSJSONSerialization.JSONObjectWithData(data, options = 0u, error = null)
        } catch (e: Exception) {
            throw ConfigurationException("Failed to parse App Store JSON response", e)
        } as? Map<*, *>
            ?: throw ConfigurationException("App Store response is not a JSON object")

        val resultCount = (json["resultCount"] as? Number)?.toInt() ?: 0
        if (resultCount == 0) return null

        val results = (json["results"] as? List<*>)
            ?: throw ConfigurationException("App Store response missing 'results' array")

        val firstResult = (results.firstOrNull() as? Map<*, *>)
            ?: throw ConfigurationException("App Store response 'results' array is empty or malformed")

        val version = (firstResult["version"] as? String)
            ?: throw ConfigurationException("App Store result missing 'version' field")

        val releaseDate = (firstResult["currentVersionReleaseDate"] as? String)
            ?: throw ConfigurationException("App Store result missing 'currentVersionReleaseDate' field")

        return AppStoreVersionInfo(
            version = version,
            currentVersionReleaseDate = releaseDate,
        )
    }
}
