package com.infinum.princeofversions

/**
 * This class parses update resource text into [BasePrinceOfVersionsConfig].
 */
public typealias ConfigurationParser = BaseConfigurationParser<String>

/**
 * This class holds loaded data from a configuration resource.
 */
public typealias PrinceOfVersionsConfig = BasePrinceOfVersionsConfig<String>

@Suppress("NotImplementedDeclaration")
internal class JvmConfigurationParser : ConfigurationParser {
    override fun parse(value: String): PrinceOfVersionsConfig {
        TODO("Not yet implemented")
    }
}
