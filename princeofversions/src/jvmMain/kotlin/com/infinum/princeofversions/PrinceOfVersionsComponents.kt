package com.infinum.princeofversions

/**
 * A data class that holds the functional components required by PrinceOfVersions for the JVM.
 *
 * This class has an internal constructor and must be created via its `Builder`.
 * This ensures that the internal dependencies between its components are always consistent.
 *
 * @property versionProvider An object that provides the current version of the application.
 * @property versionComparator An object that compares the app's current version with the
 * versions from the configuration file.
 * @property requirementCheckers A map of custom checkers that can evaluate environment-specific
 * conditions before an update is considered valid.
 * @property configurationParser An object that parses the remote configuration stream.
 * @property storage An object used to persist the last version the user was notified about.
 */
@ConsistentCopyVisibility
public data class PrinceOfVersionsComponents internal constructor(
    val versionProvider: ApplicationVersionProvider,
    val versionComparator: VersionComparator,
    val requirementCheckers: Map<String, RequirementChecker>,
    val configurationParser: ConfigurationParser,
    val storage: Storage,
) {
    /**
     * A builder for creating a consistent `PrinceOfVersionsComponents` instance for the JVM.
     */
    public class Builder {
        private var versionProvider: ApplicationVersionProvider = PropertiesApplicationVersionProvider()
        private var versionComparator: VersionComparator = JvmDefaultVersionComparator()
        private var requirementCheckers: Map<String, RequirementChecker> = mapOf(
            JvmVersionRequirementChecker.KEY to JvmVersionRequirementChecker(),
        )
        private var storage: Storage? = null
        private var configurationParser: ConfigurationParser? = null

        /**
         * Sets the version provider component.
         * @param provider An object that provides the current version of the application.
         */
        public fun withVersionProvider(provider: ApplicationVersionProvider): Builder = apply {
            this.versionProvider = provider
        }

        /**
         * Sets the version comparator component.
         * @param comparator An object that compares the app's current version with the
         * versions from the configuration file.
         */
        public fun withVersionComparator(comparator: VersionComparator): Builder = apply {
            this.versionComparator = comparator
        }

        /**
         * Sets the requirement checkers component.
         * @param checkers A map of custom checkers that can evaluate environment-specific
         * conditions before an update is considered valid.
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
         * @param storage An object used to persist the last version the user was notified about.
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
         * Assembles the final, immutable `PrinceOfVersionsComponents` object.
         *
         * @return A configured [PrinceOfVersionsComponents] instance.
         * @throws IllegalArgumentException if the custom [Storage] component is not provided.
         */
        public fun build(): PrinceOfVersionsComponents {
            val finalParser = this.configurationParser ?: JvmConfigurationParser(
                RequirementsProcessor(this.requirementCheckers),
            )

            val storage = requireNotNull(this.storage) {
                "Storage is required to build PrinceOfVersionsComponents without the mainClass parameter."
            }

            return PrinceOfVersionsComponents(
                versionProvider = this.versionProvider,
                versionComparator = this.versionComparator,
                requirementCheckers = this.requirementCheckers,
                configurationParser = finalParser,
                storage = storage,
            )
        }

        /**
         * Assembles the final, `PrinceOfVersionsComponents` object.
         *
         * @param mainClass The main application class, used to create the [JvmStorage] component.
         *
         * @return A configured [PrinceOfVersionsComponents] instance.
         */
        public fun build(mainClass: Class<*>): PrinceOfVersionsComponents {
            val finalParser = this.configurationParser ?: JvmConfigurationParser(
                RequirementsProcessor(this.requirementCheckers),
            )

            val storage = JvmStorage(mainClass)

            return PrinceOfVersionsComponents(
                versionProvider = this.versionProvider,
                versionComparator = this.versionComparator,
                requirementCheckers = this.requirementCheckers,
                configurationParser = finalParser,
                storage = storage,
            )
        }
    }
}
