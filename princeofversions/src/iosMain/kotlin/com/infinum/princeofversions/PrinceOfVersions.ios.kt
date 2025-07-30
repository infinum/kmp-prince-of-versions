package com.infinum.princeofversions

import com.infinum.princeofversions.models.Loader
import com.infinum.princeofversions.models.UpdateResult

public fun PrinceOfVersions(): PrinceOfVersions = PrinceOfVersionsImpl()

internal actual class PrinceOfVersionsImpl : PrinceOfVersions {
    actual override suspend fun checkForUpdates(source: Loader): UpdateResult {
        TODO("Not yet implemented")
    }
}
