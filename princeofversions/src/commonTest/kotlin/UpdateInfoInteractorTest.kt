import com.infinum.princeofversions.BasePrinceOfVersionsConfig
import com.infinum.princeofversions.CheckResult
import com.infinum.princeofversions.NotificationType
import com.infinum.princeofversions.UpdateInfo
import com.infinum.princeofversions.UpdateInfoInteractorImpl
import mocks.MockApplicationConfiguration
import mocks.MockConfigurationParser
import mocks.MockLoader
import mocks.MockVersionComparator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class UpdateInfoInteractorTest {

    private fun createConfig(
        mandatoryVersion: Int? = null,
        optionalVersion: Int? = null,
        optionalNotificationType: NotificationType = NotificationType.ALWAYS,
        metadata: Map<String, String> = emptyMap(),
        requirements: Map<String, String> = emptyMap(),
    ): BasePrinceOfVersionsConfig<Int> {
        return BasePrinceOfVersionsConfig(
            mandatoryVersion = mandatoryVersion,
            optionalVersion = optionalVersion,
            optionalNotificationType = optionalNotificationType,
            metadata = metadata,
            requirements = requirements,
        )
    }

    @Test
    fun `invoke should return mandatory update when mandatory version is higher than installed version`() = runTest {
        val config = createConfig(mandatoryVersion = 2)
        val configParser = MockConfigurationParser<Int>()
        configParser.setConfig(config)

        val appConfig = MockApplicationConfiguration(1)
        val loader = MockLoader()
        val versionComparator = MockVersionComparator()

        val interactor = UpdateInfoInteractorImpl(configParser, appConfig, versionComparator)

        val result = interactor.invoke(loader)

        val expectedInfo = UpdateInfo(
            requiredVersion = config.mandatoryVersion,
            lastVersionAvailable = config.optionalVersion,
            requirements = config.requirements,
            installedVersion = appConfig.version,
            notificationFrequency = config.optionalNotificationType,
        )
        val expected = CheckResult.Companion.mandatoryUpdate(config.mandatoryVersion!!, config.metadata, expectedInfo)

        assertEquals(expected, result)
    }

    @Test
    fun `invoke should return no update when mandatory version equals installed version`() = runTest {
        val config = createConfig(mandatoryVersion = 1)
        val configParser = MockConfigurationParser<Int>()
        configParser.setConfig(config)

        val appConfig = MockApplicationConfiguration(1)
        val loader = MockLoader()
        val versionComparator = MockVersionComparator()

        val interactor = UpdateInfoInteractorImpl(configParser, appConfig, versionComparator)

        val result = interactor.invoke(loader)

        val expectedInfo = UpdateInfo(
            requiredVersion = config.mandatoryVersion,
            lastVersionAvailable = config.optionalVersion,
            requirements = config.requirements,
            installedVersion = appConfig.version,
            notificationFrequency = config.optionalNotificationType,
        )
        val expected = CheckResult.Companion.noUpdate(appConfig.version, config.metadata, expectedInfo)

        assertEquals(expected, result)
    }

    @Test
    fun `invoke should return no update when both mandatory and optional versions equal installed version`() = runTest {
        val config = createConfig(mandatoryVersion = 1, optionalVersion = 1)
        val configParser = MockConfigurationParser<Int>()
        configParser.setConfig(config)

        val appConfig = MockApplicationConfiguration(1)
        val loader = MockLoader()
        val versionComparator = MockVersionComparator()

        val interactor = UpdateInfoInteractorImpl(configParser, appConfig, versionComparator)

        val result = interactor.invoke(loader)

        val expectedInfo = UpdateInfo(
            requiredVersion = config.mandatoryVersion,
            lastVersionAvailable = config.optionalVersion,
            requirements = config.requirements,
            installedVersion = appConfig.version,
            notificationFrequency = config.optionalNotificationType,
        )
        val expected = CheckResult.Companion.noUpdate(appConfig.version, config.metadata, expectedInfo)

        assertEquals(expected, result)
    }

    @Test
    fun `invoke should return mandatory update with optional version when both are available and optional is higher`() = runTest {
        val config = createConfig(mandatoryVersion = 2, optionalVersion = 3)
        val configParser = MockConfigurationParser<Int>()
        configParser.setConfig(config)

        val appConfig = MockApplicationConfiguration(1)
        val loader = MockLoader()
        val versionComparator = MockVersionComparator()

        val interactor = UpdateInfoInteractorImpl(configParser, appConfig, versionComparator)

        val result = interactor.invoke(loader)

        val expectedInfo = UpdateInfo(
            requiredVersion = config.mandatoryVersion,
            lastVersionAvailable = config.optionalVersion,
            requirements = config.requirements,
            installedVersion = appConfig.version,
            notificationFrequency = config.optionalNotificationType,
        )
        // When both mandatory and optional updates are available, and optional > mandatory,
        // we should notify about the optional version but as a mandatory update
        val expected = CheckResult.Companion.mandatoryUpdate(config.optionalVersion!!, config.metadata, expectedInfo)

        assertEquals(expected, result)
    }

    @Test
    fun `invoke should return mandatory update when mandatory and optional versions are equal and higher than installed`() = runTest {
        val config = createConfig(mandatoryVersion = 2, optionalVersion = 2)
        val configParser = MockConfigurationParser<Int>()
        configParser.setConfig(config)

        val appConfig = MockApplicationConfiguration(1)
        val loader = MockLoader()
        val versionComparator = MockVersionComparator()

        val interactor = UpdateInfoInteractorImpl(configParser, appConfig, versionComparator)

        val result = interactor.invoke(loader)

        val expectedInfo = UpdateInfo(
            requiredVersion = config.mandatoryVersion,
            lastVersionAvailable = config.optionalVersion,
            requirements = config.requirements,
            installedVersion = appConfig.version,
            notificationFrequency = config.optionalNotificationType,
        )
        val expected = CheckResult.Companion.mandatoryUpdate(config.mandatoryVersion!!, config.metadata, expectedInfo)

        assertEquals(expected, result)
    }

    @Test
    fun `invoke should return optional update when only optional version is available and higher than installed`() = runTest {
        val config = createConfig(
            optionalVersion = 2,
            optionalNotificationType = NotificationType.ONCE,
        )
        val configParser = MockConfigurationParser<Int>()
        configParser.setConfig(config)

        val appConfig = MockApplicationConfiguration(1)
        val loader = MockLoader()
        val versionComparator = MockVersionComparator()

        val interactor = UpdateInfoInteractorImpl(configParser, appConfig, versionComparator)

        val result = interactor.invoke(loader)

        val expectedInfo = UpdateInfo(
            requiredVersion = config.mandatoryVersion,
            lastVersionAvailable = config.optionalVersion,
            requirements = config.requirements,
            installedVersion = appConfig.version,
            notificationFrequency = config.optionalNotificationType,
        )
        val expected = CheckResult.Companion.optionalUpdate(
            config.optionalVersion!!,
            NotificationType.ONCE,
            config.metadata,
            expectedInfo,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `invoke should return no update when optional version equals installed version`() = runTest {
        val config = createConfig(optionalVersion = 1)
        val configParser = MockConfigurationParser<Int>()
        configParser.setConfig(config)

        val appConfig = MockApplicationConfiguration(1)
        val loader = MockLoader()
        val versionComparator = MockVersionComparator()

        val interactor = UpdateInfoInteractorImpl(configParser, appConfig, versionComparator)

        val result = interactor.invoke(loader)

        val expectedInfo = UpdateInfo(
            requiredVersion = config.mandatoryVersion,
            lastVersionAvailable = config.optionalVersion,
            requirements = config.requirements,
            installedVersion = appConfig.version,
            notificationFrequency = config.optionalNotificationType,
        )
        val expected = CheckResult.Companion.noUpdate(appConfig.version, config.metadata, expectedInfo)

        assertEquals(expected, result)
    }

    @Test
    fun `invoke should throw exception when no versions are available`() = runTest {
        val config = createConfig() // No versions set
        val configParser = MockConfigurationParser<Int>()
        configParser.setConfig(config)

        val appConfig = MockApplicationConfiguration(1)
        val loader = MockLoader()
        val versionComparator = MockVersionComparator()

        val interactor = UpdateInfoInteractorImpl(configParser, appConfig, versionComparator)

        val exception = assertFailsWith<IllegalStateException> {
            interactor.invoke(loader)
        }

        assertEquals("Both mandatory and optional version are null.", exception.message)
    }
}
