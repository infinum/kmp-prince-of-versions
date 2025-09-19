package com.infinum.princeofversions

import android.content.Context

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
    val versionComparator: VersionComparator,
    val versionProvider: ApplicationVersionProvider,
    val configurationParser: ConfigurationParser,
    val requirementCheckers: Map<String, RequirementChecker>,
    val storage: Storage,
) {

    /**
     * A builder for creating a `PrinceOfVersionsComponents` instance.
     */
    public class Builder {
        private var storage: Storage? = null
        private var configurationParser: ConfigurationParser? = null
        private var versionProvider: ApplicationVersionProvider? = null
        private var versionComparator: VersionComparator = AndroidDefaultVersionComparator()
        private var requirementCheckers: Map<String, RequirementChecker> = mapOf(
            SystemVersionRequirementChecker.KEY to SystemVersionRequirementChecker(),
        )

        /**
         * Sets the version comparator component.
         * @param comparator An object that compares the app's current version with the
         * versions from the configuration file.
         */
        public fun withVersionComparator(comparator: VersionComparator): Builder = apply {
            this.versionComparator = comparator
        }

        /**
         * Sets the version provider component.
         * @param provider An object that provides the application's current version.
         */
        public fun withVersionProvider(provider: ApplicationVersionProvider): Builder = apply {
            this.versionProvider = provider
        }

        /**
         * Sets the requirement checkers component.
         * @param checkers A map of custom checkers that can evaluate device or
         * user-specific conditions before an update is considered valid.
         * @param keepDefaultCheckers If true, the provided checkers will be added to the
         * default set. If false, they will replace the default set.
         *
         * @see SystemVersionRequirementChecker
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
        public fun withStorage(storage: Storage): Builder = apply {
            this.storage = storage
        }

        /**
         * Sets the configuration parser component.
         * @param parser An object that parses the remote configuration stream.
         */
        public fun withConfigurationParser(parser: ConfigurationParser): Builder = apply {
            this.configurationParser = parser
        }

        /**
         * Assembles the final, `PrinceOfVersionsComponents` object.
         *
         * @return A configured [PrinceOfVersionsComponents] instance.
         * @throws IllegalArgumentException if the custom [Storage] and [ApplicationVersionProvider] components
         * are not provided.
         */
        public fun build(): PrinceOfVersionsComponents {
            val configurationParser = this.configurationParser ?: AndroidConfigurationParser(
                RequirementsProcessor(this.requirementCheckers),
            )

            val versionProvider = requireNotNull(this.versionProvider) {
                "ApplicationVersionProvider is required to build PrinceOfVersionsComponents without context."
            }
            val storage = requireNotNull(this.storage) {
                "Storage is required to build PrinceOfVersionsComponents without context."
            }

            return PrinceOfVersionsComponents(
                versionComparator = this.versionComparator,
                versionProvider = versionProvider,
                configurationParser = configurationParser,
                requirementCheckers = this.requirementCheckers,
                storage = storage,
            )
        }

        /**
         * Assembles the final, `PrinceOfVersionsComponents` object.
         *
         * @param context The Android context required to construct the default
         * [AndroidStorage] and [AndroidApplicationVersionProvider] components
         *
         * @return A configured [PrinceOfVersionsComponents] instance.
         */
        public fun build(context: Context): PrinceOfVersionsComponents {
            val configurationParser = this.configurationParser ?: AndroidConfigurationParser(
                RequirementsProcessor(this.requirementCheckers),
            )
            val storage = this.storage ?: AndroidStorage(context)
            val versionProvider = this.versionProvider ?: AndroidApplicationVersionProvider(context)

            return PrinceOfVersionsComponents(
                versionComparator = this.versionComparator,
                versionProvider = versionProvider,
                requirementCheckers = this.requirementCheckers,
                configurationParser = configurationParser,
                storage = storage,
            )
        }
    }
}
