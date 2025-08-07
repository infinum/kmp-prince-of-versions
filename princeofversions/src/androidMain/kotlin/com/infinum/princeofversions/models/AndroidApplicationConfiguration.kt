package com.infinum.princeofversions.models

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * This class provides the application's version.
 *
 * @param context The application context used to retrieve package information.
 */
internal class AndroidApplicationConfiguration(context: Context) : ApplicationConfiguration<Int> {

    /**
     * The application's version code, retrieved from the package manager.
     * This implementation is backward-compatible and safely casts the version code to an Int.
     * @throws IllegalStateException if the application's package name cannot be found,
     * or if the version code is too large to be represented as an Int.
     */
    override val version: Int = try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val longVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }

        longVersionCode.toInt()
    } catch (e: PackageManager.NameNotFoundException) {
        throw kotlin.IllegalStateException("Could not find package name", e)
    }
}
