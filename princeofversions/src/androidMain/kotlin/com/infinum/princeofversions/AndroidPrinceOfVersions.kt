package com.infinum.princeofversions

import android.content.Context
import com.infinum.princeofversions.PrinceOfVersionsBase.Companion.DEFAULT_NETWORK_TIMEOUT
import kotlin.time.Duration

/**
 * Represents the main interface for using the library.
 *
 * This library checks for application updates by fetching a configuration from a given source.
 *
 */
public typealias PrinceOfVersions = PrinceOfVersionsBase<Long>

/**
 * Represents the final result of an update check.
 */
public typealias UpdateResult = BaseUpdateResult<Long>

/**
 * Creates and configures the main [PrinceOfVersions] instance.
 *
 * Uses the default components for parsing, requirements checking, version operations, and storage.
 *
 * @param context The Android context used for accessing application resources.
 *
 * @return A fully configured [PrinceOfVersions] instance, ready to be used for
 * checking for application updates.
 */
public fun PrinceOfVersions(
    context: Context,
): PrinceOfVersions = createPrinceOfVersions(
    princeOfVersionsComponents = PrinceOfVersionsComponents.Builder().build(context),
)

/**
 * Creates and configures the main [PrinceOfVersions] instance.
 *
 * Allows for custom components to be provided for parsing, requirements checking, version operations, and storage.
 *
 * @param princeOfVersionsComponents A configured [PrinceOfVersionsComponents] instance.
 * @return A fully configured [PrinceOfVersions] instance, ready to be used for
 * checking for application updates.
 */
public fun PrinceOfVersions(
    princeOfVersionsComponents: PrinceOfVersionsComponents,
): PrinceOfVersions = createPrinceOfVersions(
    princeOfVersionsComponents = princeOfVersionsComponents,
)

/**
 * Creates and configures the main [PrinceOfVersions] instance.
 *
 * Allows for custom components to be provided for parsing, requirements checking, version operations, and storage.
 *
 * @param block A lambda with receiver that allows for configuring the [PrinceOfVersionsComponents.Builder].
 *
 * **Note**: The block must construct custom [Storage] and [ApplicationVersionProvider] components. Use
 * [PrinceOfVersions(context, block)] if you want to use default implementations.
 * @return A fully configured [PrinceOfVersions] instance, ready to be used for
 * checking for application updates.
 * @throws IllegalArgumentException if the custom [Storage] and [ApplicationVersionProvider] components
 * are not provided.
 */
public fun PrinceOfVersions(
    block: PrinceOfVersionsComponents.Builder.() -> Unit,
): PrinceOfVersions = createPrinceOfVersions(
    princeOfVersionsComponents = PrinceOfVersionsComponents.Builder().apply(block).build(),
)

/**
 * Creates and configures the main [PrinceOfVersions] instance.
 *
 * Allows for custom components to be provided for parsing, requirements checking, version operations, and storage.
 *
 * @param context The Android context required to construct the default
 * [AndroidStorage] and [AndroidApplicationVersionProvider] components
 * @param block A lambda with receiver that allows for configuring the [PrinceOfVersionsComponents.Builder].
 *
 * @return A fully configured [PrinceOfVersions] instance, ready to be used for
 * checking for application updates.
 */
public fun PrinceOfVersions(
    context: Context,
    block: PrinceOfVersionsComponents.Builder.() -> Unit,
): PrinceOfVersions = createPrinceOfVersions(
    princeOfVersionsComponents = PrinceOfVersionsComponents.Builder().apply(block).build(context),
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
    private val checkForUpdatesUseCase: CheckForUpdatesUseCase<Long>,
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
public suspend fun PrinceOfVersions.checkForUpdatesFromUrl(
    url: String,
    username: String? = null,
    password: String? = null,
    networkTimeout: Duration = DEFAULT_NETWORK_TIMEOUT,
): UpdateResult = checkForUpdates(
    source = provideDefaultLoader(
        url = url,
        username = username,
        password = password,
        networkTimeout = networkTimeout,
    ),
)
