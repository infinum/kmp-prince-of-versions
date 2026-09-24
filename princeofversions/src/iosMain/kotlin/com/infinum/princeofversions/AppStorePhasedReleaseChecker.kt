package com.infinum.princeofversions

import kotlin.time.Duration.Companion.days
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.timeIntervalSince1970

internal object AppStorePhasedReleaseChecker {

    private val PHASED_RELEASE_DURATION = 7.days

    /**
     * Returns true if the version is still within its 7-day phased rollout period
     * (i.e., releaseDate + 7 days > now).
     *
     * Returns false (safe fallback, treat as fully released) if the date cannot be parsed.
     */
    fun isInPhasedRollout(releaseDateString: String): Boolean {
        val formatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
            locale = NSLocale(localeIdentifier = "en_US_POSIX")
        }

        val releaseDate = formatter.dateFromString(releaseDateString) ?: return false

        val phasedEndTimestamp = releaseDate.timeIntervalSince1970 + PHASED_RELEASE_DURATION.inWholeSeconds
        val nowTimestamp = NSDate().timeIntervalSince1970

        return phasedEndTimestamp > nowTimestamp
    }
}
