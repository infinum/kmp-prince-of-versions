@file:OptIn(ExperimentalObjCName::class)

package com.infinum.princeofversions

import com.infinum.princeofversions.PrinceOfVersionsBase.Companion.DEFAULT_NETWORK_TIMEOUT
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlin.time.Duration
import kotlinx.coroutines.CancellationException
import platform.Foundation.NSBundle

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
 * Checks for updates from the Apple App Store using the iTunes Lookup API.
 *
 * This is a zero-config check that reads the bundle ID from [NSBundle.mainBundle]
 * and queries the App Store for the latest version. App Store updates are never
 * mandatory, so the result state will be either [UpdateStatus.OPTIONAL] (update
 * available) or [UpdateStatus.NO_UPDATE].
 *
 * @param trackPhaseRelease When true, [AppStoreUpdateResult.phaseReleaseInProgress] reports whether
 *   the App Store version is still within its 7-day phased rollout window. When false,
 *   [AppStoreUpdateResult.phaseReleaseInProgress] is always false. The update is reported either way.
 * @param notificationFrequency Controls whether a previously-notified version
 *   is reported again. [NotificationType.ALWAYS] (default) always reports.
 *   [NotificationType.ONCE] suppresses repeated notifications for the same version.
 * @param networkTimeout The network timeout for the iTunes Lookup API request.
 * @param bundleId The bundle identifier to look up. When null (default), reads
 *   from [NSBundle.mainBundle.bundleIdentifier].
 * @param country The two-letter code of the App Store region to check (e.g., `"mk"`).
 *   When null (default), the iTunes Lookup API checks the U.S. App Store.
 *
 * @return An [AppStoreUpdateResult] with state [UpdateStatus.OPTIONAL] or [UpdateStatus.NO_UPDATE].
 * @throws ConfigurationException if the app is not found on the App Store, or the response is invalid.
 */
@Throws(
    IoException::class,
    ConfigurationException::class,
    CancellationException::class,
)
public suspend fun PrinceOfVersions.checkForUpdatesFromAppStore(
    trackPhaseRelease: Boolean = true,
    notificationFrequency: NotificationType = NotificationType.ALWAYS,
    networkTimeout: Duration = DEFAULT_NETWORK_TIMEOUT,
    bundleId: String? = null,
    country: String? = null,
): AppStoreUpdateResult = try {
    val resolvedBundleId = bundleId
        ?: NSBundle.mainBundle.bundleIdentifier
        ?: throw ConfigurationException("NSBundle.mainBundle.bundleIdentifier is null")

    val url = appStoreLookupUrl(bundleId = resolvedBundleId, country = country)
    val loader = provideDefaultLoader(url = url, networkTimeout = networkTimeout)
    val jsonResponse = loader.load()

    val appStoreInfo = AppStoreResponseParser.parse(jsonResponse)
        ?: throw ConfigurationException("App not found on the App Store for bundle ID: $resolvedBundleId")

    resolveAppStoreUpdate(
        appStoreInfo = appStoreInfo,
        installedVersion = IosApplicationVersionProvider().getVersion(),
        trackPhaseRelease = trackPhaseRelease,
        notificationFrequency = notificationFrequency,
        storage = IosAppStoreStorage(),
    )
} catch (e: CancellationException) {
    throw e
} catch (e: IoException) {
    throw e
} catch (e: ConfigurationException) {
    throw e
} catch (t: Throwable) {
    throw ConfigurationException(t.message ?: "Unexpected error during App Store check", t)
}

internal fun appStoreLookupUrl(
    bundleId: String,
    country: String?,
): String = buildString {
    append("$ITUNES_LOOKUP_BASE_URL?bundleId=$bundleId")
    if (country != null) append("&country=$country")
}

internal suspend fun resolveAppStoreUpdate(
    appStoreInfo: AppStoreVersionInfo,
    installedVersion: String,
    trackPhaseRelease: Boolean,
    notificationFrequency: NotificationType,
    storage: Storage,
): AppStoreUpdateResult {
    val newerVersion = appStoreInfo.version
        .takeIf { VersionParser.parseDots(it) > VersionParser.parseDots(installedVersion) }
    val releaseDate = AppStorePhasedReleaseChecker.parseReleaseDate(appStoreInfo.currentVersionReleaseDate)

    return AppStoreUpdateResult(
        updateVersion = newerVersion ?: installedVersion,
        updateState = resolveAppStoreUpdateState(newerVersion, notificationFrequency, storage),
        phaseReleaseInProgress = trackPhaseRelease &&
            releaseDate != null &&
            AppStorePhasedReleaseChecker.isInPhasedRollout(releaseDate),
        updateInfo = AppStoreUpdateInfo(
            lastVersionAvailable = appStoreInfo.version,
            installedVersion = installedVersion,
            releaseDate = releaseDate,
        ),
    )
}

private suspend fun resolveAppStoreUpdateState(
    newerVersion: String?,
    notificationFrequency: NotificationType,
    storage: Storage,
): UpdateStatus = when {
    newerVersion == null -> UpdateStatus.NO_UPDATE
    notificationFrequency == NotificationType.ONCE && storage.getLastSavedVersion() == newerVersion ->
        UpdateStatus.NO_UPDATE
    else -> {
        storage.saveVersion(newerVersion)
        UpdateStatus.OPTIONAL
    }
}

private const val ITUNES_LOOKUP_BASE_URL = "https://itunes.apple.com/lookup"

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
