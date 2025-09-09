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
 * A config data class that holds all the components needed for the Prince of Versions library to function.
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
public data class PrinceOfVersionsComponents(
    val mainClass: Class<*>,
    val versionProvider: ApplicationVersionProvider<String>,
    val versionComparator: VersionComparator<String>,
    val requirementCheckers: Map<String, RequirementChecker>,
    val configurationParser: ConfigurationParser<String>,
    val storage: Storage<String>,
) {
    public companion object {
        /**
         * Creates a [PrinceOfVersionsComponents] instance with default implementations for JVM.
         *
         * Defaults:
         * - [versionProvider]: [JvmApplicationVersionProvider], which retrieves the application's version.
         * - [versionComparator]: [JvmDefaultVersionComparator], which compares string version values.
         * - [requirementCheckers]: A map containing only [DefaultRequirementChecker].
         * - [configurationParser]: [JvmConfigurationParser], initialized with [RequirementsProcessor].
         * - [storage]: [JvmStorage], which persists data in a location derived from [mainClass].
         */
        public fun default(mainClass: Class<*>): PrinceOfVersionsComponents {
            val defaultCheckers = mapOf(DefaultRequirementChecker.KEY to DefaultRequirementChecker())

            return PrinceOfVersionsComponents(
                mainClass = mainClass,
                versionProvider = JvmApplicationVersionProvider(),
                versionComparator = JvmDefaultVersionComparator(),
                requirementCheckers = defaultCheckers,
                configurationParser = JvmConfigurationParser(RequirementsProcessor(defaultCheckers)),
                storage = JvmStorage(mainClass)
            )
        }
    }
}

