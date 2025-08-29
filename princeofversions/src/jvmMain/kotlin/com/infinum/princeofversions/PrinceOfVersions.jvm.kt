package com.infinum.princeofversions

import com.infinum.princeofversions.models.JvmApplicationConfiguration
import com.infinum.princeofversions.models.JvmConfigurationParser
import com.infinum.princeofversions.models.JvmStorage
import com.infinum.princeofversions.models.Storage
import com.infinum.princeofversions.models.UpdateResult

/**
 * Represents the main interface for using the library.
 *
 * This library checks for application updates by fetching a configuration from a given source.
 *
 */
public typealias PrinceOfVersions = PrinceOfVersionsBase<String>

/**
 * Creates and configures the main [PrinceOfVersions] instance for a JVM environment.
 *
 * @param mainClass A class reference from your application, used to create a unique storage location.
 * @param versionComparator An object that compares the app's current version with the
 * versions from the configuration file. Defaults to [JvmDefaultVersionComparator].
 * @param versionProvider An object that provides the current version of the application.
 * @param requirementCheckers A map of custom checkers that can evaluate environment-specific
 * conditions before an update is considered valid. Defaults to a map containing only
 * the [JvmDefaultRequirementChecker].
 * @param storage An object used to persist the last version the user was notified about.
 * Defaults to [JvmStorage].
 * @return A fully configured [PrinceOfVersions] instance.
 */
public fun PrinceOfVersions(
    mainClass: Class<*>,
    versionProvider: ApplicationVersionProvider<String>,
    versionComparator: VersionComparator<String> = JvmDefaultVersionComparator(),
    requirementCheckers: Map<String, RequirementChecker> = mapOf(
        JvmDefaultRequirementChecker.KEY to JvmDefaultRequirementChecker()
    ),
    storage: Storage<String> = JvmStorage(mainClass),
): PrinceOfVersions = createPrinceOfVersions(
    versionComparator = versionComparator,
    versionProvider = versionProvider,
    requirementCheckers = requirementCheckers,
    storage = storage,
)

/**
 * Creates and configures the main [PrinceOfVersions] instance for a JVM environment. Provides
 * a default [ApplicationVersionProvider] that reads the version from a properties file.
 *
 * @param mainClass A class reference from your application, used to create a unique storage location.
 * @param versionFilePath The path to the properties file containing the version information.
 * Defaults to "/version.properties".
 * @param versionKey The key in the properties file that holds the version string.
 * Defaults to "application.version".
 * @param versionComparator An object that compares the app's current version with the
 * versions from the configuration file. Defaults to [JvmDefaultVersionComparator].
 * @param requirementCheckers A map of custom checkers that can evaluate environment-specific
 * conditions before an update is considered valid. Defaults to a map containing only
 * the [JvmDefaultRequirementChecker].
 * @param storage An object used to persist the last version the user was notified about.
 * Defaults to [JvmStorage].
 * @return A fully configured [PrinceOfVersions] instance.
 *
 */
public fun PrinceOfVersions(
    mainClass: Class<*>,
    versionFilePath: String = "/version.properties",
    versionKey: String = "application.version",
    versionComparator: VersionComparator<String> = JvmDefaultVersionComparator(),
    requirementCheckers: Map<String, RequirementChecker> = mapOf(
        JvmDefaultRequirementChecker.KEY to JvmDefaultRequirementChecker()
    ),
    storage: Storage<String> = JvmStorage(mainClass),
): PrinceOfVersions = createPrinceOfVersions(
    versionComparator = versionComparator,
    versionProvider = JvmApplicationVersionProvider(versionFilePath, versionKey),
    requirementCheckers = requirementCheckers,
    storage = storage,
)

private fun createPrinceOfVersions(
    versionComparator: VersionComparator<String>,
    versionProvider: ApplicationVersionProvider<String>,
    requirementCheckers: Map<String, RequirementChecker>,
    storage: Storage<String>,
): PrinceOfVersions {
    val requirementsProcessor = RequirementsProcessor(requirementCheckers)
    val configurationParser = JvmConfigurationParser(requirementsProcessor)

    val applicationConfiguration = JvmApplicationConfiguration(versionProvider)

    val updateInfoInteractor = UpdateInfoInteractorImpl(
        configurationParser = configurationParser,
        appConfig = applicationConfiguration,
        versionComparator = versionComparator,
    )

    val checkForUpdatesUseCase = CheckForUpdatesUseCaseImpl(
        updateInfoInteractor = updateInfoInteractor,
        storage = storage,
    )

    return PrinceOfVersionsBaseImpl(checkForUpdatesUseCase = checkForUpdatesUseCase)
}

internal actual class PrinceOfVersionsBaseImpl<T>(
    private val checkForUpdatesUseCase: CheckForUpdatesUseCase<T>,
) : PrinceOfVersionsBase<T> {
    actual override suspend fun checkForUpdates(source: Loader): UpdateResult<T> =
        checkForUpdatesUseCase.checkForUpdates(source)

    actual override suspend fun checkForUpdates(
        url: String,
        username: String?,
        password: String?,
        networkTimeoutSeconds: Int
    ): UpdateResult<T> = checkForUpdates(
        JvmDefaultLoader(
            url = url,
            username = username,
            password = password,
            networkTimeoutSeconds = networkTimeoutSeconds
        )
    )
}
