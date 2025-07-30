package com.infinum.princeofversions

import android.content.Context
import com.infinum.princeofversions.models.Loader
import com.infinum.princeofversions.models.UpdateResult

public fun PrinceOfVersions(context: Context): PrinceOfVersions = PrinceOfVersionsImpl(context)

internal actual class PrinceOfVersionsImpl(context: Context) : PrinceOfVersions {
    actual override suspend fun checkForUpdates(source: Loader): UpdateResult {
        TODO("Not yet implemented")
    }
}
