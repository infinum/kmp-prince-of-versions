import com.infinum.princeofversions.CheckResult
import com.infinum.princeofversions.NotificationType
import com.infinum.princeofversions.UpdateInfo
import com.infinum.princeofversions.UpdateStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckResultTest {

    private companion object {
        private const val DEFAULT_REQUIRED_VERSION = 1
        private const val DEFAULT_LAST_VERSION_AVAILABLE = 1
        private val DEFAULT_REQUIREMENTS: Map<String, String> = emptyMap()
        private const val DEFAULT_VERSION = 1
        private val DEFAULT_METADATA: Map<String, String> = emptyMap()
        private val updateInfo = UpdateInfo(
            requiredVersion = DEFAULT_REQUIRED_VERSION,
            lastVersionAvailable = DEFAULT_LAST_VERSION_AVAILABLE,
            requirements = DEFAULT_REQUIREMENTS,
            installedVersion = DEFAULT_VERSION,
            notificationFrequency = NotificationType.ALWAYS,
        )
    }

    @Test
    fun `hasUpdate should return true when result is mandatory update`() {
        val result = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertTrue(result.hasUpdate())
    }

    @Test
    fun `hasUpdate should return true when result is optional update`() {
        val result = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ALWAYS, DEFAULT_METADATA, updateInfo)

        assertTrue(result.hasUpdate())
    }

    @Test
    fun `hasUpdate should return false when result is no update`() {
        val result = CheckResult.noUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertFalse(result.hasUpdate())
    }

    @Test
    fun `updateVersion should return correct version when result is created`() {
        val result = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(DEFAULT_VERSION, result.updateVersion)
    }

    @Test
    fun `isOptional should return false when result is mandatory update`() {
        val result = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertFalse(result.isOptional())
    }

    @Test
    fun `isOptional should return true when result is optional update`() {
        val result = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ALWAYS, DEFAULT_METADATA, updateInfo)

        assertTrue(result.isOptional())
    }

    @Test
    fun `isOptional should throw exception when result is no update`() {
        val result = CheckResult.noUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        val exception = assertFailsWith<UnsupportedOperationException> {
            result.isOptional()
        }
        assertEquals("There is no update available.", exception.message)
    }

    @Test
    fun `requireNotificationType should throw exception when result is mandatory update`() {
        val result = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        val exception = assertFailsWith<UnsupportedOperationException> {
            result.requireNotificationType()
        }
        assertEquals("There is no optional update available.", exception.message)
    }

    @Test
    fun `requireNotificationType should return ALWAYS when result is optional update with ALWAYS notification`() {
        val result = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ALWAYS, DEFAULT_METADATA, updateInfo)

        assertEquals(NotificationType.ALWAYS, result.requireNotificationType())
    }

    @Test
    fun `requireNotificationType should return ONCE when result is optional update with ONCE notification`() {
        val result = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ONCE, DEFAULT_METADATA, updateInfo)

        assertEquals(NotificationType.ONCE, result.requireNotificationType())
    }

    @Test
    fun `requireNotificationType should throw exception when result is no update`() {
        val result = CheckResult.noUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        val exception = assertFailsWith<UnsupportedOperationException> {
            result.requireNotificationType()
        }
        assertEquals("There is no update available.", exception.message)
    }

    @Test
    fun `status should be MANDATORY when result is mandatory update`() {
        val result = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(UpdateStatus.MANDATORY, result.status)
    }

    @Test
    fun `status should be OPTIONAL when result is optional update`() {
        val result = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ALWAYS, DEFAULT_METADATA, updateInfo)

        assertEquals(UpdateStatus.OPTIONAL, result.status)
    }

    @Test
    fun `status should be NO_UPDATE when result is no update`() {
        val result = CheckResult.noUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(UpdateStatus.NO_UPDATE, result.status)
    }

    @Test
    fun `metadata should be preserved when result is mandatory update`() {
        val result = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(DEFAULT_METADATA, result.metadata)
    }

    @Test
    fun `metadata should be preserved when result is optional update`() {
        val result = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ALWAYS, DEFAULT_METADATA, updateInfo)

        assertEquals(DEFAULT_METADATA, result.metadata)
    }

    @Test
    fun `metadata should be preserved when result is no update`() {
        val result = CheckResult.noUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(DEFAULT_METADATA, result.metadata)
    }

    @Test
    fun `info should be preserved when result is mandatory update`() {
        val result = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(updateInfo, result.info)
    }

    @Test
    fun `info should be preserved when result is optional update`() {
        val result = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ALWAYS, DEFAULT_METADATA, updateInfo)

        assertEquals(updateInfo, result.info)
    }

    @Test
    fun `notificationType should be null when result is mandatory update`() {
        val result = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(null, result.notificationType)
    }

    @Test
    fun `notificationType should be ALWAYS when result is optional update with ALWAYS notification`() {
        val result = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ALWAYS, DEFAULT_METADATA, updateInfo)

        assertEquals(NotificationType.ALWAYS, result.notificationType)
    }

    @Test
    fun `notificationType should be ONCE when result is optional update with ONCE notification`() {
        val result = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ONCE, DEFAULT_METADATA, updateInfo)

        assertEquals(NotificationType.ONCE, result.notificationType)
    }

    @Test
    fun `notificationType should be null when result is no update`() {
        val result = CheckResult.noUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(null, result.notificationType)
    }

    @Test
    fun `info should contain correct required version when result is mandatory update`() {
        val result = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(DEFAULT_REQUIRED_VERSION, result.info.requiredVersion)
    }

    @Test
    fun `info should contain correct required version when result is optional update`() {
        val result = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ALWAYS, DEFAULT_METADATA, updateInfo)

        assertEquals(DEFAULT_REQUIRED_VERSION, result.info.requiredVersion)
    }

    @Test
    fun `info should contain correct last version available when result is mandatory update`() {
        val result = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(DEFAULT_LAST_VERSION_AVAILABLE, result.info.lastVersionAvailable)
    }

    @Test
    fun `info should contain correct last version available when result is optional update`() {
        val result = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ALWAYS, DEFAULT_METADATA, updateInfo)

        assertEquals(DEFAULT_LAST_VERSION_AVAILABLE, result.info.lastVersionAvailable)
    }

    @Test
    fun `info should contain correct installed version when result is mandatory update`() {
        val result = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(DEFAULT_VERSION, result.info.installedVersion)
    }

    @Test
    fun `info should contain correct installed version when result is optional update`() {
        val result = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ALWAYS, DEFAULT_METADATA, updateInfo)

        assertEquals(DEFAULT_VERSION, result.info.installedVersion)
    }

    @Test
    fun `info should contain correct requirements when result is mandatory update`() {
        val result = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(DEFAULT_REQUIREMENTS, result.info.requirements)
    }

    @Test
    fun `info should contain correct requirements when result is optional update`() {
        val result = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ALWAYS, DEFAULT_METADATA, updateInfo)

        assertEquals(DEFAULT_REQUIREMENTS, result.info.requirements)
    }

    @Test
    fun `equals should return true when two results have same properties`() {
        val result1 = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)
        val result2 = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(result1, result2)
    }

    @Test
    fun `equals should return false when two results have different versions`() {
        val result1 = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)
        val result2 = CheckResult.mandatoryUpdate(2, DEFAULT_METADATA, updateInfo)

        assertFalse(result1 == result2)
    }

    @Test
    fun `equals should return false when comparing mandatory and optional results`() {
        val mandatory = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)
        val optional = CheckResult.optionalUpdate(DEFAULT_VERSION, NotificationType.ALWAYS, DEFAULT_METADATA, updateInfo)

        assertFalse(mandatory == optional)
    }

    @Test
    fun `hashCode should be equal when two results have same properties`() {
        val result1 = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)
        val result2 = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)

        assertEquals(result1.hashCode(), result2.hashCode())
    }

    @Test
    fun `toString should contain all relevant information`() {
        val result = CheckResult.mandatoryUpdate(DEFAULT_VERSION, DEFAULT_METADATA, updateInfo)
        val resultString = result.toString()

        assertTrue(resultString.contains(UpdateStatus.MANDATORY.toString()))
        assertTrue(resultString.contains(updateInfo.toString()))
        assertTrue(resultString.contains(DEFAULT_METADATA.toString()))
    }
}