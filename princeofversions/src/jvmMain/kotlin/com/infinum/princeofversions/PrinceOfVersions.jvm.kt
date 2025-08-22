package com.infinum.princeofversions

import com.infinum.princeofversions.models.UpdateResult

public fun PrinceOfVersions(): PrinceOfVersions<String> = TODO("Not yet implemented")

internal actual class PrinceOfVersionsImpl<T>(
    private val checkForUpdatesUseCase: CheckForUpdatesUseCase<T>,
) : PrinceOfVersions<T> {
    actual override suspend fun checkForUpdates(source: Loader): UpdateResult<T> =
        checkForUpdatesUseCase.checkForUpdates(source)

    actual override suspend fun checkForUpdates(
        url: String,
        username: String?,
        password: String?,
        networkTimeoutSeconds: Int
    ): UpdateResult<T> = checkForUpdates(
        JvmDefaultLoader(
            url = url,
            username = username,
            password = password,
            networkTimeoutSeconds = networkTimeoutSeconds
        )
    )
}
