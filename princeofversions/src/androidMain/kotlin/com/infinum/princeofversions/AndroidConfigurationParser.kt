package com.infinum.princeofversions

/**
 * This class parses update resource text into [PrinceOfVersionsConfig].
 */
public typealias ConfigurationParser = BaseConfigurationParser<Int>

/**
 * This class holds loaded data from a configuration resource.
 */
public typealias PrinceOfVersionsConfig = BasePrinceOfVersionsConfig<Int>

internal class AndroidConfigurationParser : ConfigurationParser {
    override fun parse(value: String): PrinceOfVersionsConfig {
        TODO("Not yet implemented")
    }
}
