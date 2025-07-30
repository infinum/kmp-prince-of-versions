package com.infinum.princeofversions.models

import com.infinum.princeofversions.enums.NotificationType
import com.infinum.princeofversions.enums.UpdateStatus

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
internal data class CheckResult private constructor(
    val status: UpdateStatus,
    val updateVersion: String,
    val notificationType: NotificationType?,
    val metadata: Map<String, String>,
    val info: UpdateInfo
) {

    companion object {

        fun mandatoryUpdate(
            version: String,
            metadata: Map<String, String>,
            updateInfo: UpdateInfo
        ): CheckResult = CheckResult(
            status = UpdateStatus.MANDATORY,
            updateVersion = version,
            notificationType = null,
            metadata = metadata,
            info = updateInfo
        )

        fun optionalUpdate(
            version: String,
            notificationType: NotificationType,
            metadata: Map<String, String>,
            updateInfo: UpdateInfo
        ): CheckResult = CheckResult(
            status = UpdateStatus.OPTIONAL,
            updateVersion = version,
            notificationType = notificationType,
            metadata = metadata,
            info = updateInfo
        )

        fun noUpdate(
            version: String,
            metadata: Map<String, String>,
            updateInfo: UpdateInfo
        ): CheckResult = CheckResult(
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
