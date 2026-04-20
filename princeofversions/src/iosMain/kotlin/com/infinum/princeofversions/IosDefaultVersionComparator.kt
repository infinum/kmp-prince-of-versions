@file:OptIn(ExperimentalObjCName::class)

package com.infinum.princeofversions

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

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

@ObjCName("makePrinceOfVersions")
public fun princeOfVersionsWithCustomParser(
    @ObjCName("parser") parser: ConfigurationParser,
): PrinceOfVersions {
    val components = PrinceOfVersionsComponents.Builder()
        .withConfigurationParser(parser)
        .build()
    return createPrinceOfVersions(components)
}

@ObjCName("makeDefaultVersionComparator")
public fun defaultIosVersionComparator(): VersionComparator = IosDefaultVersionComparator()

@ObjCName("makePrinceOfVersions")
public fun princeOfVersionsWithCustomVersionLogic(
    @ObjCName("versionProvider") provider: ApplicationVersionProvider,
    @ObjCName("comparator") comparator: VersionComparator,
): PrinceOfVersions {
    val components = PrinceOfVersionsComponents.Builder()
        .withVersionProvider(provider)
        .withVersionComparator(comparator)
        .build()
    return createPrinceOfVersions(components)
}
