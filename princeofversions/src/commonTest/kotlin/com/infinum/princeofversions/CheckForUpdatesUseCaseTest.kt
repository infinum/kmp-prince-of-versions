package com.infinum.princeofversions

import com.infinum.princeofversions.mocks.MockLoader
import com.infinum.princeofversions.mocks.MockStorage
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class CheckForUpdatesUseCaseTest {

    private lateinit var mockUpdateInfoInteractor: MockUpdateInfoInteractor<Int>
    private lateinit var mockStorage: MockStorage<Int>
    private lateinit var loader: MockLoader
    private lateinit var useCase: CheckForUpdatesUseCaseImpl<Int>

    private val defaultMetadata: Map<String, String> = emptyMap()
    private val defaultUpdateInfo = UpdateInfo(
        requiredVersion = null,
        lastVersionAvailable = null,
        requirements = emptyMap(),
        installedVersion = 5,
        notificationFrequency = NotificationType.ALWAYS,
    )

    @BeforeTest
    fun setUp() {
        mockUpdateInfoInteractor = MockUpdateInfoInteractor()
        mockStorage = MockStorage()
        loader = MockLoader()
        useCase = CheckForUpdatesUseCaseImpl(mockUpdateInfoInteractor, mockStorage)
    }

    @Test
    fun `checkForUpdates should return mandatory update and save version when mandatory update is available`() = runTest {
        val checkResult = CheckResult.mandatoryUpdate(10, defaultMetadata, defaultUpdateInfo)
        mockUpdateInfoInteractor.setCheckResult(checkResult)

        val result = useCase.checkForUpdates(loader)

        val expected = BaseUpdateResult(
            version = 10,
            status = UpdateStatus.MANDATORY,
            metadata = defaultMetadata,
        )
        assertEquals(expected, result)
        assertEquals(10, mockStorage.getLastSavedVersion())
    }

    @Test
    fun `checkForUpdates should return no update when no update is available`() = runTest {
        val checkResult = CheckResult.noUpdate(5, defaultMetadata, defaultUpdateInfo)
        mockUpdateInfoInteractor.setCheckResult(checkResult)

        val result = useCase.checkForUpdates(loader)

        val expected = BaseUpdateResult(
            version = 5,
            status = UpdateStatus.NO_UPDATE,
            metadata = defaultMetadata,
        )
        assertEquals(expected, result)
        assertEquals(null, mockStorage.getLastSavedVersion())
    }

    @Test
    fun `checkForUpdates should return optional update and save version when optional update is available for first time`() = runTest {
        val checkResult = CheckResult.optionalUpdate(12, NotificationType.ONCE, defaultMetadata, defaultUpdateInfo)
        mockUpdateInfoInteractor.setCheckResult(checkResult)
        mockStorage.clearSavedVersion() // First time seeing any update

        val result = useCase.checkForUpdates(loader)

        val expected = BaseUpdateResult(
            version = 12,
            status = UpdateStatus.OPTIONAL,
            metadata = defaultMetadata,
        )
        assertEquals(expected, result)
        assertEquals(12, mockStorage.getLastSavedVersion())
    }

    @Test
    fun `checkForUpdates should return optional update and save version when optional update is different from last notified`() = runTest {
        val checkResult = CheckResult.optionalUpdate(12, NotificationType.ONCE, defaultMetadata, defaultUpdateInfo)
        mockUpdateInfoInteractor.setCheckResult(checkResult)
        mockStorage.setSavedVersion(11) // Old version notified

        val result = useCase.checkForUpdates(loader)

        val expected = BaseUpdateResult(
            version = 12,
            status = UpdateStatus.OPTIONAL,
            metadata = defaultMetadata,
        )
        assertEquals(expected, result)
        assertEquals(12, mockStorage.getLastSavedVersion())
    }

    @Test
    fun `checkForUpdates should return optional update and save version when notification type is always even if already notified`() =
        runTest {
            val checkResult = CheckResult.optionalUpdate(12, NotificationType.ALWAYS, defaultMetadata, defaultUpdateInfo)
            mockUpdateInfoInteractor.setCheckResult(checkResult)
            mockStorage.setSavedVersion(12) // Same version already notified

            val result = useCase.checkForUpdates(loader)

            val expected = BaseUpdateResult(
                version = 12,
                status = UpdateStatus.OPTIONAL,
                metadata = defaultMetadata,
            )
            assertEquals(expected, result)
            assertEquals(12, mockStorage.getLastSavedVersion())
        }

    @Test
    fun `checkForUpdates should return no update when optional update was already notified once`() = runTest {
        val checkResult = CheckResult.optionalUpdate(12, NotificationType.ONCE, defaultMetadata, defaultUpdateInfo)
        mockUpdateInfoInteractor.setCheckResult(checkResult)
        mockStorage.setSavedVersion(12) // Same version already notified

        val result = useCase.checkForUpdates(loader)

        val expected = BaseUpdateResult(
            version = 12,
            status = UpdateStatus.NO_UPDATE,
            metadata = defaultMetadata,
        )
        assertEquals(expected, result)
        // Version should not be saved again
        assertEquals(12, mockStorage.getLastSavedVersion())
    }

    /**
     * Mock implementation of [UpdateInfoInteractor] for testing purposes.
     */
    private class MockUpdateInfoInteractor<T> : UpdateInfoInteractor<T> {
        private var checkResult: CheckResult<T>? = null

        override suspend fun invoke(loader: Loader): CheckResult<T> {
            return checkResult ?: throw IllegalStateException("CheckResult not set")
        }

        fun setCheckResult(result: CheckResult<T>) {
            checkResult = result
        }
    }
}
