package com.infinum.princeofversions

internal interface UpdateInfoInteractor<T> {
    suspend fun invoke(loader: Loader): CheckResult<T>
}

internal class UpdateInfoInteractorImpl<T>(
    private val configurationParser: BaseConfigurationParser<T>,
    private val appConfig: ApplicationConfiguration<T>,
    private val versionComparator: BaseVersionComparator<T>,
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
            mandatoryVersion != null && mandatoryVersion.isGreaterThan(currentVersion, versionComparator) -> {
                // If a mandatory update is available, it takes precedence.
                // The notified version will be the greater of the optional and mandatory versions.

                val versionToNotify = optionalVersion?.takeIf {
                    it.isGreaterThan(mandatoryVersion, versionComparator)
                } ?: mandatoryVersion

                CheckResult.mandatoryUpdate(
                    version = versionToNotify,
                    metadata = config.metadata,
                    updateInfo = updateInfo,
                )
            }
            // If no mandatory update, check for an optional update
            optionalVersion != null && optionalVersion.isGreaterThan(currentVersion, versionComparator) ->
                CheckResult.optionalUpdate(
                    version = optionalVersion,
                    notificationType = config.optionalNotificationType,
                    metadata = config.metadata,
                    updateInfo = updateInfo,
                )

            else -> {
                if (mandatoryVersion == null && optionalVersion == null) {
                    // neither mandatory nor optional version is provided
                    error("Both mandatory and optional version are null.")
                }
                // If neither mandatory nor optional update is available return no update.
                CheckResult.noUpdate(currentVersion, config.metadata, updateInfo)
            }
        }
    }
}
