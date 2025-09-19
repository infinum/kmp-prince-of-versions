package com.infinum.princeofversions

internal interface CheckForUpdatesUseCase<T> {
    suspend fun checkForUpdates(loader: Loader): BaseUpdateResult<T>
}

internal class CheckForUpdatesUseCaseImpl<T>(
    private val updateInfoInteractor: UpdateInfoInteractor<T>,
    private val storage: BaseStorage<T>,
) : CheckForUpdatesUseCase<T> {
    override suspend fun checkForUpdates(loader: Loader): BaseUpdateResult<T> {
        val checkResult = updateInfoInteractor.invoke(loader)

        return when (checkResult.status) {
            UpdateStatus.MANDATORY -> {
                storage.saveVersion(checkResult.updateVersion)
                BaseUpdateResult(
                    version = checkResult.updateVersion,
                    status = UpdateStatus.MANDATORY,
                    metadata = checkResult.metadata,
                )
            }

            UpdateStatus.OPTIONAL -> {
                val lastNotifiedVersion = storage.getLastSavedVersion()
                val isNotified = lastNotifiedVersion == checkResult.updateVersion
                val shouldNotify =
                    !isNotified || checkResult.requireNotificationType() == NotificationType.ALWAYS

                val finalStatus = if (shouldNotify) {
                    storage.saveVersion(checkResult.updateVersion)
                    UpdateStatus.OPTIONAL
                } else {
                    UpdateStatus.NO_UPDATE
                }

                BaseUpdateResult(
                    version = checkResult.updateVersion,
                    status = finalStatus,
                    metadata = checkResult.metadata,
                )
            }

            else ->
                BaseUpdateResult(
                    version = checkResult.updateVersion,
                    status = UpdateStatus.NO_UPDATE,
                    metadata = checkResult.metadata,
                )
        }
    }
}
