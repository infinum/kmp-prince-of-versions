package com.infinum.princeofversions

import com.infinum.princeofversions.models.PrinceOfVersionsConfig

/**
 * This class parses update resource text into [com.infinum.princeofversions.models.PrinceOfVersionsConfig].
 */
public interface ConfigurationParser<T> {
    /**
     * Parses update resource into [com.infinum.princeofversions.models.PrinceOfVersionsConfig].
     *
     * @param value text representation of update resource.
     * @return Class which holds all relevant data.
     * @throws Throwable if error happens during parsing.
     */
    @Throws(Throwable::class)
    public fun parse(value: String): PrinceOfVersionsConfig<T>
}
