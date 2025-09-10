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
@ConsistentCopyVisibility
public data class PrinceOfVersionsComponents internal constructor(
    val versionComparator: VersionComparator<Int>,
    val versionProvider: ApplicationVersionProvider<Int>,
    val configurationParser: ConfigurationParser<Int>,
    val requirementCheckers: Map<String, RequirementChecker>,
    val storage: Storage<Int>,
) {
    internal companion object {
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
        internal fun default(context: Context): PrinceOfVersionsComponents {
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

    /**
     * A builder for creating a `PrinceOfVersionsComponents` instance.
     */
    public class Builder(context: Context) {
        private var versionComparator: VersionComparator<Int> = AndroidDefaultVersionComparator()
        private var versionProvider: ApplicationVersionProvider<Int> = AndroidApplicationVersionProvider(context)
        private var requirementCheckers: Map<String, RequirementChecker> = mapOf(
            DefaultRequirementChecker.KEY to DefaultRequirementChecker()
        )
        private var storage: Storage<Int> = AndroidStorage(context)
        private var configurationParser: ConfigurationParser<Int>? = null

        /**
         * Sets the version comparator component.
         * @param comparator An object that compares the app's current version with the
         * versions from the configuration file.
         */
        public fun withVersionComparator(comparator: VersionComparator<Int>): Builder = apply {
            this.versionComparator = comparator
        }

        /**
         * Sets the version provider component.
         * @param provider An object that provides the application's current version.
         */
        public fun withVersionProvider(provider: ApplicationVersionProvider<Int>): Builder = apply {
            this.versionProvider = provider
        }

        /**
         * Sets the requirement checkers component.
         * @param checkers A map of custom checkers that can evaluate device or
         * user-specific conditions before an update is considered valid.
         * @param keepDefaultCheckers If true, the provided checkers will be added to the
         * default set. If false, they will replace the default set.
         */
        public fun withRequirementCheckers(
            checkers: Map<String, RequirementChecker>,
            keepDefaultCheckers: Boolean = true,
        ): Builder = apply {
            this.requirementCheckers = if (keepDefaultCheckers) {
                this.requirementCheckers + checkers
            } else {
                checkers
            }
        }

        /**
         * Sets the storage component.
         * @param storage An object used to persist data locally.
         */
        public fun withStorage(storage: Storage<Int>): Builder = apply {
            this.storage = storage
        }

        /**
         * Sets the configuration parser component.
         * @param parser An object that parses the remote configuration stream.
         */
        public fun withConfigurationParser(parser: ConfigurationParser<Int>): Builder = apply {
            this.configurationParser = parser
        }

        /**
         * Assembles the final, `PrinceOfVersionsComponents` object.
         */
        public fun build(): PrinceOfVersionsComponents {

            val configurationParser = this.configurationParser ?: AndroidConfigurationParser(
                RequirementsProcessor(this.requirementCheckers)
            )

            return PrinceOfVersionsComponents(
                versionComparator = this.versionComparator,
                versionProvider = this.versionProvider,
                requirementCheckers = this.requirementCheckers,
                configurationParser = configurationParser,
                storage = this.storage
            )
        }
    }
}
