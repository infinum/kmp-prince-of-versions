package com.infinum.princeofversions

import kotlin.time.Duration.Companion.days
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.timeIntervalSince1970

internal object AppStorePhasedReleaseChecker {

    private val PHASED_RELEASE_DURATION = 7.days

    /**
     * Parses a release date from the iTunes Lookup API (e.g., `2024-03-20T10:15:00Z`).
     *
     * Returns null if the date cannot be parsed.
     */
    fun parseReleaseDate(releaseDateString: String): NSDate? {
        val formatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
            locale = NSLocale(localeIdentifier = "en_US_POSIX")
        }

        return formatter.dateFromString(releaseDateString)
    }

    /**
     * Returns true if the version is still within its 7-day phased rollout period
     * (i.e., releaseDate + 7 days > now).
     */
    fun isInPhasedRollout(releaseDate: NSDate): Boolean {
        val phasedEndTimestamp = releaseDate.timeIntervalSince1970 + PHASED_RELEASE_DURATION.inWholeSeconds
        val nowTimestamp = NSDate().timeIntervalSince1970

        return phasedEndTimestamp > nowTimestamp
    }
}
