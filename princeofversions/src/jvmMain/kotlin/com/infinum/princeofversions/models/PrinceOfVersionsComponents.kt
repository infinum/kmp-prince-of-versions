package com.infinum.princeofversions.models

import com.infinum.princeofversions.ApplicationVersionProvider
import com.infinum.princeofversions.DefaultRequirementChecker
import com.infinum.princeofversions.JvmApplicationVersionProvider
import com.infinum.princeofversions.JvmDefaultVersionComparator
import com.infinum.princeofversions.RequirementChecker
import com.infinum.princeofversions.VersionComparator

/**
 * A config data class that holds all the components needed for the Prince of Versions library to function.
 *
 * @property mainClass A class reference from your application, used to create a unique storage location.
 * @property versionProvider An object that provides the current version of the application.
 * @property versionComparator An object that compares the app's current version with the
 * versions from the configuration file. Defaults to [JvmDefaultVersionComparator].
 * @property requirementCheckers A map of custom checkers that can evaluate environment-specific
 * conditions before an update is considered valid. Defaults to a map containing only
 * the [DefaultRequirementChecker].
 * @property storage An object used to persist the last version the user was notified about.
 * Defaults to [JvmStorage].
 */
public data class PrinceOfVersionsComponents (
    val mainClass: Class<*>,
    val versionProvider: ApplicationVersionProvider<String> = JvmApplicationVersionProvider(),
    val versionComparator: VersionComparator<String> = JvmDefaultVersionComparator(),
    val requirementCheckers: Map<String, RequirementChecker> = mapOf(
        DefaultRequirementChecker.KEY to DefaultRequirementChecker()
    ),
    val storage: Storage<String> = JvmStorage(mainClass),
)
