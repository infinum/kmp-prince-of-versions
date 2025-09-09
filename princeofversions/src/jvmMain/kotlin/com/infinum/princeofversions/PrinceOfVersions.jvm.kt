package com.infinum.princeofversions

import com.infinum.princeofversions.models.JvmApplicationConfiguration
import com.infinum.princeofversions.models.JvmConfigurationParser
import com.infinum.princeofversions.models.PrinceOfVersionsComponents
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
 * @param princeOfVersionsComponents An customizable config data class that holds all the components
 * needed for the library to work.
 * @return A fully configured [PrinceOfVersions] instance.
 */
public fun PrinceOfVersions(
    mainClass: Class<*>,
    princeOfVersionsComponents: PrinceOfVersionsComponents = PrinceOfVersionsComponents(mainClass = mainClass),
): PrinceOfVersions = createPrinceOfVersions(
    princeOfVersionsComponents = princeOfVersionsComponents,
)

private fun createPrinceOfVersions(
    princeOfVersionsComponents: PrinceOfVersionsComponents,
): PrinceOfVersions = with(princeOfVersionsComponents) {
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
