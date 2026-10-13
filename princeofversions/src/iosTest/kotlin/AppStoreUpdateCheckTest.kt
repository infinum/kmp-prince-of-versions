package com.infinum.princeofversions

import com.infinum.princeofversions.mocks.MockStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.timeZoneWithName

class AppStoreUpdateCheckTest {

    private val storage = MockStorage<String>()

    @Test
    fun `newer App Store version is reported as optional update`() = runTest {
        val result = resolve(appStoreInfo = appStoreInfo(version = "2.0.0"))

        assertEquals(UpdateStatus.OPTIONAL, result.updateState)
        assertEquals("2.0.0", result.updateVersion)
        assertEquals("2.0.0", result.updateInfo.lastVersionAvailable)
        assertEquals(INSTALLED_VERSION, result.updateInfo.installedVersion)
    }

    @Test
    fun `same App Store version reports no update with installed version`() = runTest {
        val result = resolve(appStoreInfo = appStoreInfo(version = "1.0.0"))

        assertEquals(UpdateStatus.NO_UPDATE, result.updateState)
        assertEquals(INSTALLED_VERSION, result.updateVersion)
        assertEquals("1.0.0", result.updateInfo.lastVersionAvailable)
    }

    @Test
    fun `older App Store version reports no update with installed version`() = runTest {
        val result = resolve(appStoreInfo = appStoreInfo(version = "0.9.0"))

        assertEquals(UpdateStatus.NO_UPDATE, result.updateState)
        assertEquals(INSTALLED_VERSION, result.updateVersion)
    }

    @Test
    fun `update in phased rollout is still reported when tracking phased release`() = runTest {
        val result = resolve(
            appStoreInfo = appStoreInfo(version = "2.0.0", releaseDate = releaseDateDaysAgo(1)),
            trackPhaseRelease = true,
        )

        assertEquals(UpdateStatus.OPTIONAL, result.updateState)
        assertEquals("2.0.0", result.updateVersion)
        assertTrue(result.phaseReleaseInProgress)
    }

    @Test
    fun `phased release is not in progress after 7 days`() = runTest {
        val result = resolve(
            appStoreInfo = appStoreInfo(version = "2.0.0", releaseDate = releaseDateDaysAgo(8)),
            trackPhaseRelease = true,
        )

        assertEquals(UpdateStatus.OPTIONAL, result.updateState)
        assertFalse(result.phaseReleaseInProgress)
    }

    @Test
    fun `phased release is never in progress when not tracking phased release`() = runTest {
        val result = resolve(
            appStoreInfo = appStoreInfo(version = "2.0.0", releaseDate = releaseDateDaysAgo(1)),
            trackPhaseRelease = false,
        )

        assertEquals(UpdateStatus.OPTIONAL, result.updateState)
        assertFalse(result.phaseReleaseInProgress)
    }

    @Test
    fun `unparseable release date is not in phased rollout`() = runTest {
        val result = resolve(
            appStoreInfo = appStoreInfo(version = "2.0.0", releaseDate = "not-a-date"),
            trackPhaseRelease = true,
        )

        assertFalse(result.phaseReleaseInProgress)
        assertNull(result.updateInfo.releaseDate)
    }

    @Test
    fun `release date is exposed on update info`() = runTest {
        val result = resolve(appStoreInfo = appStoreInfo(version = "2.0.0", releaseDate = "2024-03-20T10:15:00Z"))

        assertEquals(1_710_929_700.0, result.updateInfo.releaseDate?.timeIntervalSince1970)
    }

    @Test
    fun `notification frequency always reports the same version every time`() = runTest {
        val info = appStoreInfo(version = "2.0.0")

        val first = resolve(appStoreInfo = info, notificationFrequency = NotificationType.ALWAYS)
        val second = resolve(appStoreInfo = info, notificationFrequency = NotificationType.ALWAYS)

        assertEquals(UpdateStatus.OPTIONAL, first.updateState)
        assertEquals(UpdateStatus.OPTIONAL, second.updateState)
    }

    @Test
    fun `notification frequency once reports the same version only the first time`() = runTest {
        val info = appStoreInfo(version = "2.0.0")

        val first = resolve(appStoreInfo = info, notificationFrequency = NotificationType.ONCE)
        val second = resolve(appStoreInfo = info, notificationFrequency = NotificationType.ONCE)

        assertEquals(UpdateStatus.OPTIONAL, first.updateState)
        assertEquals(UpdateStatus.NO_UPDATE, second.updateState)
        assertEquals("2.0.0", second.updateVersion)
    }

    @Test
    fun `notification frequency once reports a newer version after an older one was notified`() = runTest {
        storage.setSavedVersion("2.0.0")

        val result = resolve(appStoreInfo = appStoreInfo(version = "2.1.0"), notificationFrequency = NotificationType.ONCE)

        assertEquals(UpdateStatus.OPTIONAL, result.updateState)
        assertEquals("2.1.0", storage.getLastSavedVersion())
    }

    @Test
    fun `lookup URL contains only bundle ID when country is null`() {
        assertEquals(
            "https://itunes.apple.com/lookup?bundleId=com.example.app",
            appStoreLookupUrl(bundleId = "com.example.app", country = null),
        )
    }

    @Test
    fun `lookup URL contains country when provided`() {
        assertEquals(
            "https://itunes.apple.com/lookup?bundleId=com.example.app&country=mk",
            appStoreLookupUrl(bundleId = "com.example.app", country = "mk"),
        )
    }

    private suspend fun resolve(
        appStoreInfo: AppStoreVersionInfo,
        trackPhaseRelease: Boolean = true,
        notificationFrequency: NotificationType = NotificationType.ALWAYS,
    ): AppStoreUpdateResult = resolveAppStoreUpdate(
        appStoreInfo = appStoreInfo,
        installedVersion = INSTALLED_VERSION,
        trackPhaseRelease = trackPhaseRelease,
        notificationFrequency = notificationFrequency,
        storage = storage,
    )

    private fun appStoreInfo(
        version: String,
        releaseDate: String = releaseDateDaysAgo(30),
    ) = AppStoreVersionInfo(version = version, currentVersionReleaseDate = releaseDate)

    private fun releaseDateDaysAgo(days: Int): String {
        val formatter = NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
            locale = NSLocale(localeIdentifier = "en_US_POSIX")
            timeZone = NSTimeZone.timeZoneWithName("UTC")!!
        }
        val date = NSDate.dateWithTimeIntervalSince1970(NSDate().timeIntervalSince1970 - days * ONE_DAY_SECONDS)
        return formatter.stringFromDate(date)
    }

    companion object {
        private const val INSTALLED_VERSION = "1.0.0-1"
        private const val ONE_DAY_SECONDS = 86_400.0
    }
}
