package com.infinum.princeofversions.models

/**
 * This class parses update resource text into [PrinceOfVersionsConfig].
 */
internal interface ConfigurationParser {
    /**
     * Parses update resource into [PrinceOfVersionsConfig].
     *
     * @param value text representation of update resource.
     * @return Class which holds all relevant data.
     * @throws Throwable if error happens during parsing.
     */
    @Throws(Throwable::class)
    fun parse(value: String): PrinceOfVersionsConfig
}
