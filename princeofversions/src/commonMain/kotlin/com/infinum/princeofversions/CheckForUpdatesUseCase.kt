package com.infinum.princeofversions

import com.infinum.princeofversions.enums.NotificationType
import com.infinum.princeofversions.enums.UpdateStatus
import com.infinum.princeofversions.models.Storage
import com.infinum.princeofversions.models.UpdateResult

internal interface CheckForUpdatesUseCase {
    suspend fun checkForUpdates(): UpdateResult
}

internal class CheckForUpdatesUseCaseImpl(
    private val updateInfoInteractor: UpdateInfoInteractor,
    private val storage: Storage
) : CheckForUpdatesUseCase {
    override suspend fun checkForUpdates(): UpdateResult {
        val checkResult = updateInfoInteractor.execute()

        return when (checkResult.status) {
            UpdateStatus.MANDATORY -> {
                storage.saveVersion(checkResult.updateVersion)
                UpdateResult(
                    version = checkResult.updateVersion,
                    status = UpdateStatus.MANDATORY,
                    metadata = checkResult.metadata,
                )
            }
            UpdateStatus.OPTIONAL -> {
                val lastNotifiedVersion = storage.getLastSavedVersion()
                val isNotified = lastNotifiedVersion == checkResult.updateVersion
                val shouldNotify = !isNotified || checkResult.safeNotificationType() == NotificationType.ALWAYS

                val finalStatus = if (shouldNotify) {
                    storage.saveVersion(checkResult.updateVersion)
                    UpdateStatus.OPTIONAL
                } else {
                    UpdateStatus.NO_UPDATE
                }

                UpdateResult(
                    version = checkResult.updateVersion,
                    status = finalStatus,
                    metadata = checkResult.metadata,
                )
            }
            else -> {
                UpdateResult(
                    version = checkResult.updateVersion,
                    status = UpdateStatus.NO_UPDATE,
                    metadata = checkResult.metadata,
                )
            }
        }
    }
}
