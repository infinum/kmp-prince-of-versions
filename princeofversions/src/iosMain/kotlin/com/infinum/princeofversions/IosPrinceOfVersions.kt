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

public fun PrinceOfVersions(): PrinceOfVersions = createPrinceOfVersions(
    princeOfVersionsComponents = PrinceOfVersionsComponents.Builder().build(),
)

internal class PrinceOfVersionsImpl(
    private val checkForUpdatesUseCase: CheckForUpdatesUseCase<String>,
) : PrinceOfVersions {
    override suspend fun checkForUpdates(source: Loader): UpdateResult =
        checkForUpdatesUseCase.checkForUpdates(source)
}

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
    ),
)

private fun createPrinceOfVersions(
    princeOfVersionsComponents: PrinceOfVersionsComponents,
): PrinceOfVersions =
    with(princeOfVersionsComponents) {
        val applicationConfiguration = IosApplicationConfiguration(versionProvider = versionProvider)

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

/**
 * Starts a check for an update, loading the configuration from a URL (iOS actual).
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
