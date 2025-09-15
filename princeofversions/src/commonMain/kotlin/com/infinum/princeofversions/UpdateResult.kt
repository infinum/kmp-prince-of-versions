package com.infinum.princeofversions

/**
 * Represents the final result of an update check.
 *
 * This data class encapsulates all the information needed to act on the
 * result of a Prince of Versions check, such as showing a dialog to the user.
 *
 * @property version The version string of the available update. In the case of [UpdateStatus.NO_UPDATE], this will
 * be the currently installed version of the application.
 * @property status The final status of the update check, indicating whether an update is available, mandatory,
 * or if there is no update.
 * @property metadata A map of metadata associated with the resolved update configuration. This is provided even
 * if no update is available.
 */
public data class UpdateResult<T>(
    public val version: T,
    public val status: UpdateStatus,
    public val metadata: Map<String, String> = emptyMap()
)
