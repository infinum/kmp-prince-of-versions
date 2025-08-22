package com.infinum.princeofversions

import android.content.Context
import com.infinum.princeofversions.models.UpdateResult

@Suppress("unused")
public fun PrinceOfVersions(context: Context): PrinceOfVersions<Int> = TODO("Not yet implemented")

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
        AndroidDefaultLoader(
            url = url,
            username = username,
            password = password,
            networkTimeoutSeconds = networkTimeoutSeconds
        )
    )
}
