package com.infinum.princeofversions

/**
 * Intermediate result of update check.
 *
 * This result contains:
 * - Update status ([UpdateStatus])
 * - Update version
 * - Notification type ([NotificationType])
 * - Metadata from the update configuration
 * - General update information ([UpdateInfo])
 */
@ConsistentCopyVisibility
internal data class CheckResult<T> private constructor(
    val status: UpdateStatus,
    val updateVersion: T,
    val notificationType: NotificationType?,
    val metadata: Map<String, String>,
    val info: UpdateInfo<T>
) {

    companion object {

        fun <T> mandatoryUpdate(
            version: T,
            metadata: Map<String, String>,
            updateInfo: UpdateInfo<T>,
        ): CheckResult<T> = CheckResult(
            status = UpdateStatus.MANDATORY,
            updateVersion = version,
            notificationType = null,
            metadata = metadata,
            info = updateInfo
        )

        fun <T> optionalUpdate(
            version: T,
            notificationType: NotificationType,
            metadata: Map<String, String>,
            updateInfo: UpdateInfo<T>,
        ): CheckResult<T> = CheckResult(
            status = UpdateStatus.OPTIONAL,
            updateVersion = version,
            notificationType = notificationType,
            metadata = metadata,
            info = updateInfo
        )

        fun <T> noUpdate(
            version: T,
            metadata: Map<String, String>,
            updateInfo: UpdateInfo<T>,
        ): CheckResult<T> = CheckResult(
            status = UpdateStatus.NO_UPDATE,
            updateVersion = version,
            notificationType = null,
            metadata = metadata,
            info = updateInfo
        )
    }

    /**
     * Checks if the result indicates that an update (either optional or mandatory) is available.
     * @return True if an update is available, false otherwise.
     */
    fun hasUpdate(): Boolean =
        status == UpdateStatus.MANDATORY || status == UpdateStatus.OPTIONAL

    /**
     * Checks if the available update is optional.
     * @return True if the update is optional.
     * @throws UnsupportedOperationException if no update is available.
     */
    fun isOptional(): Boolean =
        if (hasUpdate()) status == UpdateStatus.OPTIONAL
        else throw UnsupportedOperationException("There is no update available.")

    /**
     * Safely returns the notification type for an optional update.
     * @return The notification type if the update is optional.
     * @throws UnsupportedOperationException if the update is not optional.
     */
    fun safeNotificationType(): NotificationType? =
        if (isOptional()) notificationType
        else throw UnsupportedOperationException("There is no optional update available.")
}
