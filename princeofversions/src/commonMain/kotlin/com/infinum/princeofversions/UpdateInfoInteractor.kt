package com.infinum.princeofversions

import com.infinum.princeofversions.models.ApplicationConfiguration
import com.infinum.princeofversions.models.CheckResult
import com.infinum.princeofversions.Loader
import com.infinum.princeofversions.models.UpdateInfo

internal interface UpdateInfoInteractor<T> {
    suspend fun invoke(loader: Loader): CheckResult<T>
}

internal class UpdateInfoInteractorImpl<T>(
    private val configurationParser: ConfigurationParser<T>,
    private val appConfig: ApplicationConfiguration<T>,
    private val versionComparator: VersionComparator<T>,
) : UpdateInfoInteractor<T> {

    override suspend fun invoke(loader: Loader): CheckResult<T> {

        val configJson = loader.load()
        val currentVersion = appConfig.version

        val config = configurationParser.parse(configJson)

        val updateInfo = UpdateInfo(
            requiredVersion = config.mandatoryVersion,
            lastVersionAvailable = config.optionalVersion,
            requirements = config.requirements,
            installedVersion = currentVersion,
            notificationFrequency = config.optionalNotificationType,
        )

        val mandatoryVersion = config.mandatoryVersion
        val optionalVersion = config.optionalVersion

        return when {
            // Check for mandatory update first
            mandatoryVersion != null && versionComparator.isVersionGreaterThan(mandatoryVersion, currentVersion) -> {
                // If a mandatory update is available, it takes precedence.
                // The notified version will be the greater of the optional and mandatory versions.

                val versionToNotify = optionalVersion?.takeIf {
                    versionComparator.isVersionGreaterThan(it, mandatoryVersion)
                } ?: mandatoryVersion

                CheckResult.mandatoryUpdate(
                    version = versionToNotify,
                    metadata = config.metadata,
                    updateInfo = updateInfo,
                )
            }
            // If no mandatory update, check for an optional update
            optionalVersion != null && versionComparator.isVersionGreaterThan(optionalVersion, currentVersion) -> {
                CheckResult.optionalUpdate(
                    version = optionalVersion,
                    notificationType = config.optionalNotificationType,
                    metadata = config.metadata,
                    updateInfo = updateInfo,
                )
            }
            else -> {
                check(
                    mandatoryVersion == null && optionalVersion == null
                ) { "Both mandatory and optional version are null." }
                // If neither mandatory nor optional update is available return no update.
                CheckResult.noUpdate(currentVersion, config.metadata, updateInfo)
            }
        }
    }
}
