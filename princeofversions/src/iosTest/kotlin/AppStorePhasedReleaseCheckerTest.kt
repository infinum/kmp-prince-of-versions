package com.infinum.princeofversions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970

class AppStorePhasedReleaseCheckerTest {

    private fun dateSecondsAgo(seconds: Double): NSDate =
        NSDate.dateWithTimeIntervalSince1970(NSDate().timeIntervalSince1970 - seconds)

    @Test
    fun `returns true when release is within 7-day window`() {
        assertTrue(AppStorePhasedReleaseChecker.isInPhasedRollout(dateSecondsAgo(ONE_DAY_SECONDS)))
    }

    @Test
    fun `returns false when release is older than 7 days`() {
        assertFalse(AppStorePhasedReleaseChecker.isInPhasedRollout(dateSecondsAgo(8 * ONE_DAY_SECONDS)))
    }

    @Test
    fun `returns false when release is exactly 7 days old`() {
        assertFalse(AppStorePhasedReleaseChecker.isInPhasedRollout(dateSecondsAgo(7 * ONE_DAY_SECONDS)))
    }

    @Test
    fun `returns true for release just now`() {
        assertTrue(AppStorePhasedReleaseChecker.isInPhasedRollout(dateSecondsAgo(0.0)))
    }

    @Test
    fun `parses iTunes Lookup API date with literal Z`() {
        val date = AppStorePhasedReleaseChecker.parseReleaseDate("2024-03-20T10:15:00Z")

        assertEquals(RELEASE_DATE_2024_03_20_10_15_UTC, date?.timeIntervalSince1970)
    }

    @Test
    fun `parses date with numeric UTC offset`() {
        val date = AppStorePhasedReleaseChecker.parseReleaseDate("2024-03-20T12:15:00+0200")

        assertEquals(RELEASE_DATE_2024_03_20_10_15_UTC, date?.timeIntervalSince1970)
    }

    @Test
    fun `returns null for unparseable date string`() {
        assertNull(AppStorePhasedReleaseChecker.parseReleaseDate("not-a-date"))
    }

    @Test
    fun `returns null for empty date string`() {
        assertNull(AppStorePhasedReleaseChecker.parseReleaseDate(""))
    }

    companion object {
        private const val ONE_DAY_SECONDS = 86_400.0
        private const val RELEASE_DATE_2024_03_20_10_15_UTC = 1_710_929_700.0
    }
}
