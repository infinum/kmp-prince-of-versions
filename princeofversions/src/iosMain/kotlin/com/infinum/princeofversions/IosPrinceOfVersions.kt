package com.infinum.princeofversions

import com.infinum.princeofversions.PrinceOfVersionsBase.Companion.DEFAULT_NETWORK_TIMEOUT
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

public fun createPrinceOfVersions(): PrinceOfVersions = createPrinceOfVersions(
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
@Throws(
    IoException::class,
    RequirementsNotSatisfiedException::class,
    ConfigurationException::class,
    CancellationException::class,
)
public suspend fun PrinceOfVersions.checkForUpdatesFromUrl(
    url: String,
    username: String? = null,
    password: String? = null,
    networkTimeout: Duration = DEFAULT_NETWORK_TIMEOUT,
): UpdateResult = try {
    checkForUpdates(
        source = provideDefaultLoader(
            url = url,
            username = username,
            password = password,
            networkTimeout = networkTimeout,
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
 * mandatory, so the result status will be either [UpdateStatus.OPTIONAL] (update
 * available) or [UpdateStatus.NO_UPDATE].
 *
 * @param trackPhaseRelease When true, versions still within their 7-day phased
 *   rollout window are treated as not yet available.
 * @param notificationFrequency Controls whether a previously-notified version
 *   is reported again. [NotificationType.ONCE] (default) suppresses repeated
 *   notifications for the same version. [NotificationType.ALWAYS] always reports.
 * @param networkTimeout The network timeout for the iTunes Lookup API request.
 * @param bundleId The bundle identifier to look up. When null (default), reads
 *   from [NSBundle.mainBundle.bundleIdentifier].
 *
 * @return An [UpdateResult] with status [UpdateStatus.OPTIONAL] or [UpdateStatus.NO_UPDATE].
 */
@Throws(
    IoException::class,
    ConfigurationException::class,
    CancellationException::class,
)
public suspend fun PrinceOfVersions.checkForUpdatesFromAppStore(
    trackPhaseRelease: Boolean = false,
    notificationFrequency: NotificationType = NotificationType.ONCE,
    networkTimeout: Duration = DEFAULT_NETWORK_TIMEOUT,
    bundleId: String? = null,
): UpdateResult = try {
    val resolvedBundleId = bundleId
        ?: NSBundle.mainBundle.bundleIdentifier
        ?: throw ConfigurationException("NSBundle.mainBundle.bundleIdentifier is null")

    val url = "$ITUNES_LOOKUP_BASE_URL?bundleId=$resolvedBundleId"
    val loader = provideDefaultLoader(url = url, networkTimeout = networkTimeout)
    val jsonResponse = loader.load()

    val appStoreInfo = AppStoreResponseParser.parse(jsonResponse)

    if (appStoreInfo == null) {
        BaseUpdateResult(
            version = installedVersionString(),
            status = UpdateStatus.NO_UPDATE,
        )
    } else {
        resolveAppStoreUpdate(appStoreInfo, trackPhaseRelease, notificationFrequency)
    }
} catch (e: CancellationException) {
    throw e
} catch (e: IoException) {
    throw e
} catch (e: ConfigurationException) {
    throw e
} catch (t: Throwable) {
    throw ConfigurationException(t.message ?: "Unexpected error during App Store check", t)
}

private suspend fun resolveAppStoreUpdate(
    appStoreInfo: AppStoreVersionInfo,
    trackPhaseRelease: Boolean,
    notificationFrequency: NotificationType,
): UpdateResult {
    if (trackPhaseRelease &&
        AppStorePhasedReleaseChecker.isInPhasedRollout(appStoreInfo.currentVersionReleaseDate)
    ) {
        return BaseUpdateResult(
            version = installedVersionString(),
            status = UpdateStatus.NO_UPDATE,
        )
    }

    val installedVersion = VersionParser.parseDots(installedVersionString())
    val appStoreVersion = VersionParser.parseDots(appStoreInfo.version)

    if (appStoreVersion <= installedVersion) {
        return BaseUpdateResult(
            version = installedVersionString(),
            status = UpdateStatus.NO_UPDATE,
        )
    }

    return applyAppStoreNotificationFrequency(appStoreInfo.version, notificationFrequency)
}

private suspend fun applyAppStoreNotificationFrequency(
    appStoreVersion: String,
    notificationFrequency: NotificationType,
): UpdateResult {
    val storage = IosAppStoreStorage()
    val lastNotified = storage.getLastSavedVersion()
    val alreadyNotified = lastNotified == appStoreVersion

    val status = if (!alreadyNotified || notificationFrequency == NotificationType.ALWAYS) {
        storage.saveVersion(appStoreVersion)
        UpdateStatus.OPTIONAL
    } else {
        UpdateStatus.NO_UPDATE
    }

    return BaseUpdateResult(
        version = appStoreVersion,
        status = status,
    )
}

private fun installedVersionString(): String =
    IosApplicationVersionProvider().getVersion()

private const val ITUNES_LOOKUP_BASE_URL = "https://itunes.apple.com/lookup"

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
    return createPrinceOfVersions(components)
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
