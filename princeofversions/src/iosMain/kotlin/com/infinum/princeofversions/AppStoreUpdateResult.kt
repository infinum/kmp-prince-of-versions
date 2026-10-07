package com.infinum.princeofversions

import platform.Foundation.NSDate

/**
 * Represents the result of an App Store update check.
 *
 * Mirrors `AppStoreUpdateResult` from the native ios-prince-of-versions library.
 *
 * @property updateVersion The newest version the app can update to. If the App Store has no newer
 * version, this is the installed version.
 * @property updateState [UpdateStatus.OPTIONAL] if an update should be shown, otherwise [UpdateStatus.NO_UPDATE].
 * App Store updates are never [UpdateStatus.MANDATORY].
 * @property phaseReleaseInProgress True while the App Store version is less than 7 days old and phased release
 * tracking is enabled. Always false when phased release tracking is disabled. The App Store does not report when a
 * phased release finishes early, so this stays true for the full 7 days.
 * @property updateInfo The App Store and installed version data used for the check.
 */
public data class AppStoreUpdateResult(
    public val updateVersion: String,
    public val updateState: UpdateStatus,
    public val phaseReleaseInProgress: Boolean,
    public val updateInfo: AppStoreUpdateInfo,
)

/**
 * Represents the version data used for an App Store update check.
 *
 * Mirrors `AppStoreUpdateInfo` from the native ios-prince-of-versions library.
 *
 * @property lastVersionAvailable The latest version on the App Store.
 * @property installedVersion The installed version, in the `CFBundleShortVersionString-CFBundleVersion` format.
 * @property releaseDate The release date of [lastVersionAvailable], or null if it is unknown.
 */
public data class AppStoreUpdateInfo(
    public val lastVersionAvailable: String,
    public val installedVersion: String,
    public val releaseDate: NSDate?,
)
