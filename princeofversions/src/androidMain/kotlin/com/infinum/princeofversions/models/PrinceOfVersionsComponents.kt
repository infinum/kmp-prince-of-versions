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
import kotlin.Int

/**
 * A data class that holds the functional components required by PrinceOfVersions.
 *
 * @property versionComparator An object that compares the app's current version with the
 * versions from the configuration file.
 * @property versionProvider An object that provides the application's current version.
 * @property configurationParser An object that parses the remote configuration stream.
 * @property requirementCheckers A map of custom checkers that can evaluate device or
 * user-specific conditions before an update is considered valid.
 * @property storage An object used to persist data locally.
 */
public data class PrinceOfVersionsComponents(
    val versionComparator: VersionComparator<Int>,
    val versionProvider: ApplicationVersionProvider<Int>,
    val configurationParser: ConfigurationParser<Int>,
    val requirementCheckers: Map<String, RequirementChecker>,
    val storage: Storage<Int>,
) {
    public companion object {
        /**
         * Creates a [PrinceOfVersionsComponents] instance with default implementations for Android.
         *
         * Defaults:
         * - [versionComparator]: [AndroidDefaultVersionComparator], which compares integer version codes.
         * - [versionProvider]: [AndroidApplicationVersionProvider], which retrieves the app's version code
         * from the application's package manager.
         * - [requirementCheckers]: A map containing only [DefaultRequirementChecker].
         * - [configurationParser]: [AndroidConfigurationParser], initialized with [RequirementsProcessor].
         * - [storage]: [AndroidStorage], which uses Jetpack DataStore for persistence.
         */
        public fun default(context: Context): PrinceOfVersionsComponents {
            val defaultCheckers = mapOf(DefaultRequirementChecker.KEY to DefaultRequirementChecker())

            return PrinceOfVersionsComponents(
                versionComparator = AndroidDefaultVersionComparator(),
                versionProvider = AndroidApplicationVersionProvider(context),
                requirementCheckers = defaultCheckers,
                configurationParser = AndroidConfigurationParser(RequirementsProcessor(defaultCheckers)),
                storage = AndroidStorage(context)
            )
        }
    }
}
