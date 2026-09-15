package com.infinum.princeofversions

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.timeIntervalSince1970

class AppStorePhasedReleaseCheckerTest {

    private fun formatDate(timestampSeconds: Double): String {
        val formatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
            locale = NSLocale(localeIdentifier = "en_US_POSIX")
        }
        return formatter.stringFromDate(NSDate(timeIntervalSinceReferenceDate = timestampSeconds - REFERENCE_DATE_OFFSET))
    }

    @Test
    fun `returns true when release is within 7-day window`() {
        val now = NSDate().timeIntervalSince1970
        val oneDayAgo = now - ONE_DAY_SECONDS
        val dateString = formatDate(oneDayAgo)

        assertTrue(AppStorePhasedReleaseChecker.isInPhasedRollout(dateString))
    }

    @Test
    fun `returns false when release is older than 7 days`() {
        val now = NSDate().timeIntervalSince1970
        val eightDaysAgo = now - (8 * ONE_DAY_SECONDS)
        val dateString = formatDate(eightDaysAgo)

        assertFalse(AppStorePhasedReleaseChecker.isInPhasedRollout(dateString))
    }

    @Test
    fun `returns false when release is exactly 7 days old`() {
        val now = NSDate().timeIntervalSince1970
        val sevenDaysAgo = now - (7 * ONE_DAY_SECONDS)
        val dateString = formatDate(sevenDaysAgo)

        assertFalse(AppStorePhasedReleaseChecker.isInPhasedRollout(dateString))
    }

    @Test
    fun `returns false for unparseable date string`() {
        assertFalse(AppStorePhasedReleaseChecker.isInPhasedRollout("not-a-date"))
    }

    @Test
    fun `returns false for empty date string`() {
        assertFalse(AppStorePhasedReleaseChecker.isInPhasedRollout(""))
    }

    @Test
    fun `returns true for release just now`() {
        val now = NSDate().timeIntervalSince1970
        val dateString = formatDate(now)

        assertTrue(AppStorePhasedReleaseChecker.isInPhasedRollout(dateString))
    }

    companion object {
        private const val ONE_DAY_SECONDS = 86_400.0
        // NSDate reference date (2001-01-01) offset from Unix epoch (1970-01-01)
        private const val REFERENCE_DATE_OFFSET = 978_307_200.0
    }
}
