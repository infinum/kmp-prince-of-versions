package com.infinum.princeofversions

public fun PrinceOfVersions(): PrinceOfVersions = PrinceOfVersionsImpl()

internal actual class PrinceOfVersionsImpl : PrinceOfVersions {
    actual override suspend fun checkForUpdates(source: Loader): UpdateResult {
        TODO("Not yet implemented")
    }
}
