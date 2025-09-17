package com.infinum.princeofversions

import android.content.Context
import kotlin.time.Duration

/**
 * Represents the main interface for using the library.
 *
 * This library checks for application updates by fetching a configuration from a given source.
 *
 */
public typealias PrinceOfVersions = PrinceOfVersionsBase<Int>

/**
 * Represents the final result of an update check.
 */
public typealias UpdateResult = BaseUpdateResult<Int>

/**
 * Creates and configures the main [PrinceOfVersions] instance.
 *
 * Uses the default components for parsing, requirements checking, version operations, and storage.
 *
 * @param context The Android context used for accessing application resources.
 *
 * @return A fully configured [PrinceOfVersions] instance, ready to be used for
 * checking for application updates.
 * @see PrinceOfVersions
 */
public fun PrinceOfVersions(
    context: Context,
): PrinceOfVersions = createPrinceOfVersions(
    princeOfVersionsComponents = PrinceOfVersionsComponents.Builder(context).build(),
)

/**
 * Creates and configures the main [PrinceOfVersions] instance.
 *
 * Allows for custom components to be provided for parsing, requirements checking, version operations, and storage.
 *
 * @return A fully configured [PrinceOfVersions] instance, ready to be used for
 * checking for application updates.
 * @see PrinceOfVersions
 */
public fun PrinceOfVersions(
    princeOfVersionsComponents: PrinceOfVersionsComponents,
): PrinceOfVersions = createPrinceOfVersions(
    princeOfVersionsComponents = princeOfVersionsComponents,
)

private fun createPrinceOfVersions(
    princeOfVersionsComponents: PrinceOfVersionsComponents,
): PrinceOfVersions =
    with(princeOfVersionsComponents) {
        val applicationConfiguration = AndroidApplicationConfiguration(versionProvider = versionProvider)

        val updateInfoInteractor = UpdateInfoInteractorImpl(
            configurationParser = configurationParser,
            appConfig = applicationConfiguration,
            versionComparator = versionComparator,
        )

        val checkForUpdatesUseCase = CheckForUpdatesUseCaseImpl(
            updateInfoInteractor = updateInfoInteractor,
            storage = storage,
        )
        return PrinceOfVersionsImpl(checkForUpdatesUseCase = checkForUpdatesUseCase)
    }

internal class PrinceOfVersionsImpl(
    private val checkForUpdatesUseCase: CheckForUpdatesUseCase<Int>,
) : PrinceOfVersions {
    override suspend fun checkForUpdates(source: Loader): UpdateResult =
        checkForUpdatesUseCase.checkForUpdates(source)
}

/**
 * Starts a check for an update.
 *
 * @param url The network url from which to load the update configuration
 * @param username Optional username for basic authentication.
 * @param password Optional password for basic authentication.
 * @param networkTimeout Network timeout. Default is 60 seconds.
 *
 * @return An [UpdateResult] instance that contains the result of the update check.
 */
public suspend fun PrinceOfVersions.checkForUpdates(
    url: String,
    username: String?,
    password: String?,
    networkTimeout: Duration,
): UpdateResult = checkForUpdates(
    source = provideDefaultLoader(
        url = url,
        username = username,
        password = password,
        networkTimeout = networkTimeout,
    )
)
