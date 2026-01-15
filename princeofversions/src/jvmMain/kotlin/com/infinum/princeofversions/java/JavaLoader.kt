package com.infinum.princeofversions.java

import kotlin.jvm.Throws

/**
 * This interface loads an update resource and is meant for use in Java projects. In Kotlin projects, use [com.infinum.princeofversions.Loader].
 */
public interface JavaLoader {

    /**
     * Loads update resource into [String].
     *
     * @return Loaded text.
     * @throws Throwable if an error happens during loading.
     */
    @Throws(Throwable::class)
    public fun load(): String
}
