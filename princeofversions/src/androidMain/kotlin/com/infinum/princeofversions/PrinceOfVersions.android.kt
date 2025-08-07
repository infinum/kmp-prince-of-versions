package com.infinum.princeofversions

import android.content.Context
import com.infinum.princeofversions.models.AndroidApplicationConfiguration
import com.infinum.princeofversions.models.AndroidConfigurationParser
import com.infinum.princeofversions.models.AndroidStorage
import com.infinum.princeofversions.models.Storage
import com.infinum.princeofversions.models.UpdateResult

/**
 * Creates and configures the main [PrinceOfVersions] instance.
 *
 * This factory function is the primary entry point for using the library on Android.
 *
 * @param context The application context.
 * @param versionComparator An object that compares the app's current version with the
 * versions from the configuration file. Defaults to [AndroidDefaultVersionComparator],
 * which compares integer version codes.
 * @param requirementCheckers A map of custom checkers that can evaluate device or
 * user-specific conditions before an update is considered valid.
 * Defaults to a map containing only the [DefaultRequirementChecker].
 * @param storage An object used to persist the last version the user was notified about,
 * which controls the notification frequency for optional updates.
 * Defaults to [AndroidStorage], which uses Jetpack DataStore.
 * @return A fully configured [PrinceOfVersions] instance, ready to be used for
 * checking for application updates.
 * @see PrinceOfVersions
 * @see UpdateResult
 */
public fun PrinceOfVersions(
    context: Context,
    versionComparator: VersionComparator<Int> = AndroidDefaultVersionComparator(),
    requirementCheckers: Map<String, RequirementChecker> = mapOf(
        DefaultRequirementChecker.KEY to DefaultRequirementChecker()
    ),
    storage: Storage<Int> = AndroidStorage(context),
): PrinceOfVersions<Int> {
    val requirementsProcessor = RequirementsProcessor(requirementCheckers)
    val configurationParser = AndroidConfigurationParser(requirementsProcessor)

    val applicationConfiguration = AndroidApplicationConfiguration(context)

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

internal actual class PrinceOfVersionsImpl<T>(
    private val checkForUpdatesUseCase: CheckForUpdatesUseCase<T>,
) : PrinceOfVersions<T> {
    actual override suspend fun checkForUpdates(source: Loader): UpdateResult<T> =
        checkForUpdatesUseCase.checkForUpdates(source)
}
