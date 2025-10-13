package com.infinum.princeofversions

internal class IosApplicationConfiguration(
    versionProvider: ApplicationVersionProvider,
) : ApplicationConfiguration<String> {
    override val version: String = versionProvider.getVersion()
}
