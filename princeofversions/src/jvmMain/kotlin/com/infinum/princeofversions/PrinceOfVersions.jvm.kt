package com.infinum.princeofversions

import com.infinum.princeofversions.models.JvmApplicationConfiguration
import com.infinum.princeofversions.models.JvmConfigurationParser
import com.infinum.princeofversions.models.JvmStorage
import com.infinum.princeofversions.models.Storage
import com.infinum.princeofversions.models.UpdateResult

/**
 * Creates and configures the main [PrinceOfVersions] instance for a JVM environment.
 *
 * This factory function is the primary entry point for using the library on the desktop.
 *
 * @param mainClass A class reference from your application, used to create a unique storage location.
 * @param versionFilePath The path to the properties file containing the app version.
 * @param versionKey The key for the version property within the properties file.
 * @param versionComparator An object that compares the app's current version with the
 * versions from the configuration file. Defaults to [JvmDefaultVersionComparator].
 * @param requirementCheckers A map of custom checkers that can evaluate environment-specific
 * conditions before an update is considered valid. Defaults to a map containing only
 * the [JvmDefaultRequirementChecker].
 * @param storage An object used to persist the last version the user was notified about.
 * Defaults to [JvmStorage].
 * @return A fully configured [PrinceOfVersions] instance.
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
): PrinceOfVersions<String> {
    val requirementsProcessor = RequirementsProcessor(requirementCheckers)
    val configurationParser = JvmConfigurationParser(requirementsProcessor)

    val applicationConfiguration = JvmApplicationConfiguration(
        filePath = versionFilePath,
        versionKey = versionKey
    )

    val updateInfoInteractor = UpdateInfoInteractorImpl<String>(
        configurationParser = configurationParser,
        appConfig = applicationConfiguration,
        versionComparator = versionComparator,
    )

    val checkForUpdatesUseCase = CheckForUpdatesUseCaseImpl<String>(
        updateInfoInteractor = updateInfoInteractor,
        storage = storage,
    )

    return PrinceOfVersionsImpl<String>(checkForUpdatesUseCase = checkForUpdatesUseCase)
}

internal actual class PrinceOfVersionsImpl<T>(
    private val checkForUpdatesUseCase: CheckForUpdatesUseCase<T>,
) : PrinceOfVersions<T> {
    actual override suspend fun checkForUpdates(source: Loader): UpdateResult<T> =
        checkForUpdatesUseCase.checkForUpdates(source)
}
