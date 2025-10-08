package com.infinum.princeofversions

@ConsistentCopyVisibility
public data class PrinceOfVersionsComponents internal constructor(
    val versionComparator: VersionComparator,
    val versionProvider: ApplicationVersionProvider,
    val configurationParser: ConfigurationParser,
    val requirementCheckers: Map<String, RequirementChecker>,
    val storage: Storage,
) {

    public class Builder {
        private var versionComparator: VersionComparator = IosDefaultVersionComparator()
        private var versionProvider: ApplicationVersionProvider = IosApplicationVersionProvider()
        private var configurationParser: ConfigurationParser? = null
        private var requirementCheckers: Map<String, RequirementChecker> = mapOf(
            SystemVersionRequirementChecker.KEY to SystemVersionRequirementChecker(),
        )
        private var storage: Storage = IosStorage()

        public fun withVersionComparator(comparator: VersionComparator): Builder = apply {
            this.versionComparator = comparator
        }

        public fun withVersionProvider(provider: ApplicationVersionProvider): Builder = apply {
            this.versionProvider = provider
        }

        /**
         * @param keepDefaultCheckers If true, merges with defaults (OS version). If false, replaces them.
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

        public fun withStorage(storage: Storage): Builder = apply {
            this.storage = storage
        }

        public fun withConfigurationParser(parser: ConfigurationParser): Builder = apply {
            this.configurationParser = parser
        }

        public fun build(): PrinceOfVersionsComponents {
            val finalParser = this.configurationParser ?: IosConfigurationParser(
                RequirementsProcessor(this.requirementCheckers),
            )

            return PrinceOfVersionsComponents(
                versionComparator = this.versionComparator,
                versionProvider = this.versionProvider,
                configurationParser = finalParser,
                requirementCheckers = this.requirementCheckers,
                storage = this.storage,
            )
        }
    }
}