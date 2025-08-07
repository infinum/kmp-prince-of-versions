package com.infinum.princeofversions

import com.infinum.princeofversions.models.PrinceOfVersionsConfig

/**
 * This class parses update resource text into [com.infinum.princeofversions.models.PrinceOfVersionsConfig].
 */
internal interface ConfigurationParser<T> {
    /**
     * Parses update resource into [com.infinum.princeofversions.models.PrinceOfVersionsConfig].
     *
     * @param value text representation of update resource.
     * @return Class which holds all relevant data.
     * @throws Throwable if error happens during parsing.
     */
    @Throws(Throwable::class)
    fun parse(value: String): PrinceOfVersionsConfig<T>
}
