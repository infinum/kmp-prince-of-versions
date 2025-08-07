package com.infinum.princeofversions

import android.os.Build

/**
 * The default Android [RequirementChecker] implementation.
 */
internal actual class DefaultRequirementChecker : RequirementChecker {

    /**
     * Checks if the device's OS version meets the required minimum version.
     *
     * @param value The minimum required OS version as a String.
     * @return true if the device's OS version is greater than or equal to the required version.
     */
    actual override fun checkRequirements(value: String): Boolean {
        val minSdk = value.toInt()
        return minSdk <= Build.VERSION.SDK_INT
    }

    companion object {
        const val KEY = "required_os_version"
    }
}
