package com.infinum.princeofversions

/**
 * This class parses update resource text into [BasePrinceOfVersionsConfig].
 */
public interface BaseConfigurationParser<T> {
    /**
     * Parses update resource into [BasePrinceOfVersionsConfig].
     *
     * @param value text representation of update resource.
     * @return Class which holds all relevant data.
     * @throws Throwable if error happens during parsing.
     */
    @Throws(Throwable::class)
    public fun parse(value: String): BasePrinceOfVersionsConfig<T>
}
