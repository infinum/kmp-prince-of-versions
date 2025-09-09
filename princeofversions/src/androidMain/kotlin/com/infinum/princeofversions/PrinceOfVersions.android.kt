package com.infinum.princeofversions

import PrinceOfVersionsComponents
import android.content.Context
import com.infinum.princeofversions.models.AndroidApplicationConfiguration
import com.infinum.princeofversions.models.UpdateResult

/**
 * Represents the main interface for using the library.
 *
 * This library checks for application updates by fetching a configuration from a given source.
 *
 */
public typealias PrinceOfVersions = PrinceOfVersionsBase<Int>

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
 * @see UpdateResult
 */
public fun PrinceOfVersions(
    context: Context,
): PrinceOfVersions = createPrinceOfVersions(
    princeOfVersionsComponents = PrinceOfVersionsComponents.default(context = context),
)

/**
 * Creates and configures the main [PrinceOfVersions] instance.
 *
 * Allows for custom components to be provided for parsing, requirements checking, version operations, and storage.
 *
 * @return A fully configured [PrinceOfVersions] instance, ready to be used for
 * checking for application updates.
 * @see PrinceOfVersions
 * @see UpdateResult
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
        AndroidDefaultLoader(
            url = url,
            username = username,
            password = password,
            networkTimeoutSeconds = networkTimeoutSeconds
        )
    )
}
