package com.infinum.princeofversions

import android.os.Build

internal class SystemVersionRequirementChecker : RequirementChecker {

    /**
     * Checks if the device's OS version meets the required minimum version.
     *
     * @param value The minimum required OS version as a String.
     * @return true if the device's OS version is greater than or equal to the required version.
     */
    override fun checkRequirements(value: String?): Boolean {
        val minSdk = value?.toInt() ?: return false
        return minSdk <= Build.VERSION.SDK_INT
    }

    companion object {
        const val KEY = "required_os_version"
    }
}
