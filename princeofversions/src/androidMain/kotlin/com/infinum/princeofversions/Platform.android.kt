package com.infinum.princeofversions

import android.os.Build

private class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

public actual fun getPlatform(): Platform = AndroidPlatform()