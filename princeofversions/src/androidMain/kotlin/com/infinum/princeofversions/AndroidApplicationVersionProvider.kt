package com.infinum.princeofversions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat

/**
 * Provides the current version of the application.
 */
public typealias ApplicationVersionProvider = BaseApplicationVersionProvider<Long>

internal class AndroidApplicationVersionProvider(context: Context) : ApplicationVersionProvider {

    private val version: Long = try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        PackageInfoCompat.getLongVersionCode(packageInfo)
    } catch (e: PackageManager.NameNotFoundException) {
        throw kotlin.IllegalStateException("Could not find package name", e)
    }

    override fun getVersion(): Long = version
}
