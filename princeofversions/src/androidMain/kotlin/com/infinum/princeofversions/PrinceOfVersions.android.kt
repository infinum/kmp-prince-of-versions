package com.infinum.princeofversions

import android.content.Context
import kotlin.time.Duration

/**
 * Represents the main interface for using the library.
 *
 * This library checks for application updates by fetching a configuration from a given source.
 *
 */
public typealias PrinceOfVersions = PrinceOfVersionsBase<Int>

@Suppress("unused")
public fun PrinceOfVersions(context: Context): PrinceOfVersions = TODO("Not yet implemented")

internal actual class PrinceOfVersionsBaseImpl<T>(
    private val checkForUpdatesUseCase: CheckForUpdatesUseCase<T>,
) : PrinceOfVersionsBase<T> {
    actual override suspend fun checkForUpdates(source: Loader): UpdateResult<T> =
        checkForUpdatesUseCase.checkForUpdates(source)
}

public actual suspend fun <T> PrinceOfVersionsBase<T>.checkForUpdates(
    url: String,
    username: String?,
    password: String?,
    networkTimeout: Duration,
): UpdateResult<T> = checkForUpdates(
    source = AndroidDefaultLoader(
        url = url,
        username = username,
        password = password,
        networkTimeout = networkTimeout,
    )
)
