package com.infinum.princeofversions

import com.infinum.princeofversions.models.ApplicationConfiguration
import com.infinum.princeofversions.models.CheckResult
import com.infinum.princeofversions.models.ConfigurationParser
import com.infinum.princeofversions.models.Loader
import com.infinum.princeofversions.models.UpdateInfo

internal interface UpdateInfoInteractor {
    suspend fun execute(): CheckResult
}

internal class UpdateInfoInteractorImpl(
    private val configurationParser: ConfigurationParser,
    private val appConfig: ApplicationConfiguration,
    private val loader: Loader,
) : UpdateInfoInteractor {

    override suspend fun execute(): CheckResult {

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
            mandatoryVersion != null && mandatoryVersion.isVersionGreater(currentVersion) -> {
                // If a mandatory update is available, it takes precedence.
                // The notified version will be the greater of the optional and mandatory versions.

                val versionToNotify = optionalVersion?.takeIf {
                    mandatoryVersion.isVersionGreater(it)
                } ?: mandatoryVersion

                CheckResult.mandatoryUpdate(
                    version = versionToNotify,
                    metadata = config.metadata,
                    updateInfo = updateInfo,
                )
            }
            // If no mandatory update, check for an optional update
            optionalVersion != null && optionalVersion.isVersionGreater(currentVersion) -> {
                CheckResult.optionalUpdate(
                    version = optionalVersion,
                    notificationType = config.optionalNotificationType,
                    metadata = config.metadata,
                    updateInfo = updateInfo,
                )
            }
            else -> {
                // If neither mandatory nor optional update is available return no update.
                check(
                    !(mandatoryVersion == null && optionalVersion == null)
                ) { "Both mandatory and optional version are null." }
                CheckResult.noUpdate(currentVersion, config.metadata, updateInfo)
            }
        }
    }
}
