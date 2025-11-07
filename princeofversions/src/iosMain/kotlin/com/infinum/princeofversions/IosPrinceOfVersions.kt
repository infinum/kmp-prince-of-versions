package com.infinum.princeofversions

import com.infinum.princeofversions.PrinceOfVersionsBase.Companion.DEFAULT_NETWORK_TIMEOUT
import kotlinx.coroutines.CancellationException
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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

// Add a Swift-friendly overload that takes milliseconds
public class ConfigurationException(message: String) : Exception(message)

@Throws(
    IoException::class,
    RequirementsNotSatisfiedException::class,
    ConfigurationException::class,
    CancellationException::class
)
public suspend fun PrinceOfVersions.checkForUpdatesFromUrlMillis(
    url: String,
    username: String? = null,
    password: String? = null,
    networkTimeoutMillis: Long = DEFAULT_NETWORK_TIMEOUT.inWholeMilliseconds,
): UpdateResult = try {
    checkForUpdates(
        source = provideDefaultLoader(
            url = url,
            username = username,
            password = password,
            networkTimeout = networkTimeoutMillis.toDuration(DurationUnit.MILLISECONDS),
        ),
    )
} catch (e: CancellationException) {
    throw e
}catch (e: IllegalStateException) {
    throw ConfigurationException(e.message ?: "Invalid configuration")
} catch (e: RequirementsNotSatisfiedException) {
    throw e
} catch (e: IoException) {
    throw e
} catch (t: Throwable) {
    throw ConfigurationException(t.message ?: "Unexpected error")
}

/**
 * Convenience for Swift: build PoV with a single custom checker.
 */
public fun princeOfVersionsWithCustomChecker(
    key: String,
    checker: RequirementChecker,
    keepDefaultCheckers: Boolean = true,
): PrinceOfVersions {
    val components = PrinceOfVersionsComponents.Builder()
        .withRequirementCheckers(mapOf(key to checker), keepDefaultCheckers)
        .build()
    return createPrinceOfVersions(components) // ensure createPrinceOfVersions is internal or public in same file
}

/**
 * Convenience for Swift: build PoV with many custom checkers.
 * In Swift you can pass an array of KotlinPair<String, RequirementChecker>.
 */
public fun princeOfVersionsWithCustomCheckers(
    pairs: Array<kotlin.Pair<String, RequirementChecker>>,
    keepDefaultCheckers: Boolean = true,
): PrinceOfVersions {
    val map = pairs.toMap()
    val components = PrinceOfVersionsComponents.Builder()
        .withRequirementCheckers(map, keepDefaultCheckers)
        .build()
    return createPrinceOfVersions(components)
}
