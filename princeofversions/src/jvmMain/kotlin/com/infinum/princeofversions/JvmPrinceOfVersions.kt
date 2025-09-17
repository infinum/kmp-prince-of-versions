package com.infinum.princeofversions

import com.infinum.princeofversions.PrinceOfVersionsBase.Companion.DEFAULT_NETWORK_TIMEOUT
import kotlin.time.Duration

/**
 * Represents the main interface for using the library.
 *
 * This library checks for application updates by fetching a configuration from a given source.
 *
 */
public typealias PrinceOfVersions = PrinceOfVersionsBase<String>

/**
 * Represents the final result of an update check.
 */
public typealias UpdateResult = BaseUpdateResult<String>

/**
 * Creates and configures the main [PrinceOfVersions] instance.
 *
 * @param mainClass A class reference from your application, used to create a unique storage location.
 * needed for the library to work.
 * @return A fully configured [PrinceOfVersions] instance.
 */
public fun PrinceOfVersions(
    mainClass: Class<*>,
): PrinceOfVersions = createPrinceOfVersions(
    princeOfVersionsComponents = PrinceOfVersionsComponents.Builder().build(mainClass),
)

/**
 * Creates and configures the main [PrinceOfVersions] instance.
 *
 * @param princeOfVersionsComponents A customizable config data class that holds all the components
 *
 * @return A fully configured [PrinceOfVersions] instance.
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
 * **Note**: The block must construct a custom [Storage] component. Use
 * [PrinceOfVersions(context, block)] if you want to use the default implementations.
 * @return A fully configured [PrinceOfVersions] instance, ready to be used for
 * checking for application updates.
 * @throws IllegalArgumentException if the custom [Storage] component
 * is not provided.
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
 * @param mainClass The main class of the application, used to create a default [JvmStorage] component.
 * @param block A lambda with receiver that allows for configuring the [PrinceOfVersionsComponents.Builder].
 *
 * @return A fully configured [PrinceOfVersions] instance, ready to be used for
 * checking for application updates.
 */
public fun PrinceOfVersions(
    mainClass: Class<*>,
    block: PrinceOfVersionsComponents.Builder.() -> Unit,
): PrinceOfVersions = createPrinceOfVersions(
    princeOfVersionsComponents = PrinceOfVersionsComponents.Builder().apply(block).build(mainClass),
)

private fun createPrinceOfVersions(
    princeOfVersionsComponents: PrinceOfVersionsComponents,
): PrinceOfVersions = with(princeOfVersionsComponents) {
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

    return PrinceOfVersionsImpl(checkForUpdatesUseCase = checkForUpdatesUseCase)
}

internal class PrinceOfVersionsImpl(
    private val checkForUpdatesUseCase: CheckForUpdatesUseCase<String>,
) : PrinceOfVersions {
    override suspend fun checkForUpdates(source: Loader): UpdateResult =
        checkForUpdatesUseCase.checkForUpdates(source)
}

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
    )
)
