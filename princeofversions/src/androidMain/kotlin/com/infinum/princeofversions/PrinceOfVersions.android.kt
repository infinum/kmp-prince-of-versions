package com.infinum.princeofversions

import android.content.Context

public fun PrinceOfVersions(context: Context): PrinceOfVersions = PrinceOfVersionsImpl(context)

internal actual class PrinceOfVersionsImpl(context: Context) : PrinceOfVersions {
    actual override suspend fun checkForUpdates(source: Loader): UpdateResult {
        TODO("Not yet implemented")
    }
}
