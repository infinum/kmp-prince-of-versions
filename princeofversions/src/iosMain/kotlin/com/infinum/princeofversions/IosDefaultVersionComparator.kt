package com.infinum.princeofversions

public typealias VersionComparator = BaseVersionComparator<String>

internal class IosDefaultVersionComparator : VersionComparator {

    override fun compare(firstVersion: String, secondVersion: String): Int {
        val v1 = VersionParser.parseWithBuild(firstVersion)
        val v2 = VersionParser.parseWithBuild(secondVersion)

        return compareValuesBy(
            v1,
            v2,
            { it.version.major },
            { it.version.minor },
            { it.version.patch },
            { it.build },
        )
    }
}

public fun princeOfVersionsWithCustomParser(
    parser: ConfigurationParser,
): PrinceOfVersions {
    val components = PrinceOfVersionsComponents.Builder()
        .withConfigurationParser(parser)
        .build()
    return createPrinceOfVersions(components)
}

public fun defaultIosVersionComparator(): VersionComparator = IosDefaultVersionComparator()

public fun princeOfVersionsWithCustomVersionLogic(
    provider: ApplicationVersionProvider,
    comparator: VersionComparator,
): PrinceOfVersions {
    val components = PrinceOfVersionsComponents.Builder()
        .withVersionProvider(provider)
        .withVersionComparator(comparator)
        .build()
    return createPrinceOfVersions(components)
}
