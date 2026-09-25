@file:OptIn(ExperimentalObjCName::class)

package com.infinum.princeofversions

import com.infinum.princeofversions.PrinceOfVersionsBase.Companion.DEFAULT_NETWORK_TIMEOUT
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlin.time.Duration
import kotlinx.coroutines.CancellationException

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

@ObjCName("makePrinceOfVersions")
public fun createPrinceOfVersions(): PrinceOfVersions = createPrinceOfVersions(
    princeOfVersionsComponents = PrinceOfVersionsComponents.Builder().build(),
)

internal class PrinceOfVersionsImpl(
    private val checkForUpdatesUseCase: CheckForUpdatesUseCase<String>,
) : PrinceOfVersions {
    override suspend fun checkForUpdates(source: Loader): UpdateResult =
        checkForUpdatesUseCase.checkForUpdates(source)
}

internal fun createPrinceOfVersions(
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
@ObjCName("checkForUpdates")
@Throws(
    IoException::class,
    RequirementsNotSatisfiedException::class,
    ConfigurationException::class,
    CancellationException::class,
)
public suspend fun PrinceOfVersions.checkForUpdatesFromUrl(
    @ObjCName("from") url: String,
    @ObjCName("username") username: String? = null,
    @ObjCName("password") password: String? = null,
    @ObjCName("timeout") networkTimeout: Duration = DEFAULT_NETWORK_TIMEOUT,
): UpdateResult = checkForUpdatesFromUrl(
    url = url,
    headers = emptyMap(),
    username = username,
    password = password,
    networkTimeout = networkTimeout,
)

/**
 * Starts a check for an update, sending [headers] with the configuration request (iOS actual).
 *
 * Use this to authenticate with an API key or bearer token without implementing a custom [Loader].
 * If [username] and [password] are provided, their basic authentication `Authorization` header
 * replaces any `Authorization` entry in [headers].
 *
 * This is a separate overload rather than a new parameter because Swift callers pass every
 * argument explicitly, so a new parameter would break existing call sites.
 */
@ObjCName("checkForUpdates")
@Throws(
    IoException::class,
    RequirementsNotSatisfiedException::class,
    ConfigurationException::class,
    CancellationException::class,
)
public suspend fun PrinceOfVersions.checkForUpdatesFromUrl(
    @ObjCName("from") url: String,
    @ObjCName("headers") headers: Map<String, String>,
    @ObjCName("username") username: String? = null,
    @ObjCName("password") password: String? = null,
    @ObjCName("timeout") networkTimeout: Duration = DEFAULT_NETWORK_TIMEOUT,
): UpdateResult = try {
    checkForUpdates(
        source = provideDefaultLoader(
            url = url,
            username = username,
            password = password,
            networkTimeout = networkTimeout,
            headers = headers,
        ),
    )
} catch (e: CancellationException) {
    throw e
} catch (e: IllegalStateException) {
    throw ConfigurationException(e.message ?: "Invalid configuration", e)
} catch (e: RequirementsNotSatisfiedException) {
    throw e
} catch (e: IoException) {
    throw e
} catch (t: Throwable) {
    throw ConfigurationException(t.message ?: "Unexpected error", t)
}

public class ConfigurationException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Convenience for Swift: build PoV with a single custom checker.
 */
@ObjCName("makePrinceOfVersions")
public fun princeOfVersionsWithCustomChecker(
    @ObjCName("checkerKey") key: String,
    @ObjCName("checker") checker: RequirementChecker,
    @ObjCName("keepDefaultCheckers") keepDefaultCheckers: Boolean = true,
): PrinceOfVersions {
    val components = PrinceOfVersionsComponents.Builder()
        .withRequirementCheckers(mapOf(key to checker), keepDefaultCheckers)
        .build()
    return createPrinceOfVersions(components)
}

/**
 * Convenience for Swift: build PoV with many custom checkers.
 * In Swift you can pass an array of KotlinPair<String, RequirementChecker>.
 */
@ObjCName("makePrinceOfVersions")
public fun princeOfVersionsWithCustomCheckers(
    @ObjCName("checkers") pairs: Array<kotlin.Pair<String, RequirementChecker>>,
    @ObjCName("keepDefaultCheckers") keepDefaultCheckers: Boolean = true,
): PrinceOfVersions {
    val map = pairs.toMap()
    val components = PrinceOfVersionsComponents.Builder()
        .withRequirementCheckers(map, keepDefaultCheckers)
        .build()
    return createPrinceOfVersions(components)
}
