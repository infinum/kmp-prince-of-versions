import android.content.Context
import com.infinum.princeofversions.AndroidApplicationVersionProvider
import com.infinum.princeofversions.AndroidDefaultVersionComparator
import com.infinum.princeofversions.ApplicationVersionProvider
import com.infinum.princeofversions.ConfigurationParser
import com.infinum.princeofversions.DefaultRequirementChecker
import com.infinum.princeofversions.RequirementChecker
import com.infinum.princeofversions.RequirementsProcessor
import com.infinum.princeofversions.VersionComparator
import com.infinum.princeofversions.models.AndroidConfigurationParser
import com.infinum.princeofversions.models.AndroidStorage
import com.infinum.princeofversions.models.Storage

/**
 * A data class that holds the functional components required by PrinceOfVersions.
 *
 * @property versionComparator An object that compares the app's current version with the
 * versions from the configuration file. Defaults to [AndroidDefaultVersionComparator],
 * which compares integer version codes.
 * @property versionProvider An object that provides the application's current version.
 * Defaults to [AndroidApplicationVersionProvider].
 * @property configurationParser An object that parses the remote configuration stream.
 * Defaults to [AndroidConfigurationParser].
 * @property requirementCheckers A map of custom checkers that can evaluate device or
 * user-specific conditions before an update is considered valid.
 * Defaults to a map containing only the [DefaultRequirementChecker].
 * @property storage An object used to persist data locally.
 * Defaults to [AndroidStorage], which uses Jetpack DataStore.
 */
@ConsistentCopyVisibility
public data class PrinceOfVersionsComponents internal constructor(
    val versionComparator: VersionComparator<Int>,
    val versionProvider: ApplicationVersionProvider<Int>,
    val configurationParser: ConfigurationParser<Int>,
    val requirementCheckers: Map<String, RequirementChecker>,
    val storage: Storage<Int>,
) {
    public companion object {
        /**
         * Creates a configuration of components, using the provided `Context` to
         * initialize the default Android-specific implementations.
         *
         * @param context The application context.
         * @param versionComparator An object that compares the app's versions.
         * Defaults to [AndroidDefaultVersionComparator], which compares integer version codes.
         * @param versionProvider An object that provides the application's current version.
         * Defaults to [AndroidApplicationVersionProvider].
         * @param configurationParser An object that parses the remote configuration stream.
         * Defaults to [AndroidConfigurationParser].
         * @param requirementCheckers A map of custom checkers that can evaluate device or
         * user-specific conditions before an update is considered valid.
         * Defaults to a map containing only the [DefaultRequirementChecker].
         * @param storage An object used to persist data locally.
         * Defaults to [AndroidStorage], which uses DataStore.
         * @return A `PrinceOfVersionsComponents` instance configured with the specified components.
         */
        @Suppress("LongParameterList")
        public operator fun invoke(
            context: Context,
            versionComparator: VersionComparator<Int> = AndroidDefaultVersionComparator(),
            versionProvider: ApplicationVersionProvider<Int> = AndroidApplicationVersionProvider(context),
            requirementCheckers: Map<String, RequirementChecker> = mapOf(
                DefaultRequirementChecker.KEY to DefaultRequirementChecker()
            ),
            configurationParser: ConfigurationParser<Int> = AndroidConfigurationParser(
                RequirementsProcessor(requirementCheckers)
            ),
            storage: Storage<Int> = AndroidStorage(context)
        ): PrinceOfVersionsComponents {
            return PrinceOfVersionsComponents(
                versionComparator = versionComparator,
                versionProvider = versionProvider,
                configurationParser = configurationParser,
                requirementCheckers = requirementCheckers,
                storage = storage
            )
        }
    }
}
