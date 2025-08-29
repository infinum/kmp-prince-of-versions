package com.infinum.princeofversions

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

public class AndroidApplicationVersionProvider(context: Context) : ApplicationVersionProvider<Int> {
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
