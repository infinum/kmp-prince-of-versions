package com.infinum.princeofversions

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Provides the current version of the application.
 */
public typealias ApplicationVersionProvider = BaseApplicationVersionProvider<Int>

internal class AndroidApplicationVersionProvider(context: Context) : ApplicationVersionProvider {

    private val version: Int = try {
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

    override fun getVersion(): Int = version
}
