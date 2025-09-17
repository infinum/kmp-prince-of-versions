package com.infinum.princeofversions.models

import com.infinum.princeofversions.ApplicationVersionProvider
import com.infinum.princeofversions.ConfigurationParser
import com.infinum.princeofversions.DefaultRequirementChecker
import com.infinum.princeofversions.JvmApplicationVersionProvider
import com.infinum.princeofversions.JvmDefaultVersionComparator
import com.infinum.princeofversions.RequirementChecker
import com.infinum.princeofversions.RequirementsProcessor
import com.infinum.princeofversions.VersionComparator

/**
 * A data class that holds the functional components required by PrinceOfVersions for the JVM.
 *
 * This class has an internal constructor and must be created via its `Builder`.
 * This ensures that the internal dependencies between its components are always consistent.
 *
 * @property mainClass A class reference from your application, used to create a unique storage location.
 * @property versionProvider An object that provides the current version of the application.
 * @property versionComparator An object that compares the app's current version with the
 * versions from the configuration file.
 * @property requirementCheckers A map of custom checkers that can evaluate environment-specific
 * conditions before an update is considered valid.
 * @property configurationParser An object that parses the remote configuration stream.
 * @property storage An object used to persist the last version the user was notified about.
 */
public data class PrinceOfVersionsComponents internal constructor(
    val mainClass: Class<*>,
    val versionProvider: ApplicationVersionProvider<String>,
    val versionComparator: VersionComparator<String>,
    val requirementCheckers: Map<String, RequirementChecker>,
    val configurationParser: ConfigurationParser<String>,
    val storage: Storage<String>,
) {
    /**
     * A builder for creating a consistent `PrinceOfVersionsComponents` instance for the JVM.
     * @param mainClass A class reference from your application, used to create a unique storage location.
     */
    public class Builder(private val mainClass: Class<*>) {
        private var versionProvider: ApplicationVersionProvider<String> = JvmApplicationVersionProvider()
        private var versionComparator: VersionComparator<String> = JvmDefaultVersionComparator()
        private var requirementCheckers: Map<String, RequirementChecker> = mapOf(
            DefaultRequirementChecker.KEY to DefaultRequirementChecker()
        )
        private var storage: Storage<String> = JvmStorage(mainClass)
        private var configurationParser: ConfigurationParser<String>? = null

        /**
         * Sets the version provider component.
         * @param provider An object that provides the current version of the application.
         */
        public fun withVersionProvider(provider: ApplicationVersionProvider<String>): Builder = apply {
            this.versionProvider = provider
        }

        /**
         * Sets the version comparator component.
         * @param comparator An object that compares the app's current version with the
         * versions from the configuration file.
         */
        public fun withVersionComparator(comparator: VersionComparator<String>): Builder = apply {
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
        public fun withStorage(storage: Storage<String>): Builder = apply {
            this.storage = storage
        }

        /**
         * Sets the configuration parser component.
         * @param parser An object that parses the remote configuration stream.
         */
        public fun withConfigurationParser(parser: ConfigurationParser<String>): Builder = apply {
            this.configurationParser = parser
        }

        /**
         * Assembles the final, immutable `PrinceOfVersionsComponents` object.
         */
        public fun build(): PrinceOfVersionsComponents {
            val finalParser = this.configurationParser ?: JvmConfigurationParser(
                RequirementsProcessor(this.requirementCheckers)
            )

            return PrinceOfVersionsComponents(
                mainClass = this.mainClass,
                versionProvider = this.versionProvider,
                versionComparator = this.versionComparator,
                requirementCheckers = this.requirementCheckers,
                configurationParser = finalParser,
                storage = this.storage
            )
        }
    }
}
