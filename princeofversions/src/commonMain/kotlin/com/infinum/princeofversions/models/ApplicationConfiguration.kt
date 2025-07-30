package com.infinum.princeofversions.models

/**
 * Provides application parameters such as version and SDK level.
 */
internal interface ApplicationConfiguration {
    /**
     * The application's version code.
     */
    val version: String

    /**
     * The device's SDK version code.
     */
    val sdkVersionCode: String
}
