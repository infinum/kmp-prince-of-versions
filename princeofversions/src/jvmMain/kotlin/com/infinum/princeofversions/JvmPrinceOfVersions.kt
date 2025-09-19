package com.infinum.princeofversions

import kotlin.time.Duration

/**
 * Represents the main interface for using the library.
 *
 * This library checks for application updates by fetching a configuration from a given source.
 *
 */
public typealias PrinceOfVersions = PrinceOfVersionsBase<String>

/**
 * Represents the final result of an update check.
 */
public typealias UpdateResult = BaseUpdateResult<String>

@Suppress("NotImplementedDeclaration")
public fun PrinceOfVersions(): PrinceOfVersions = TODO("Not yet implemented")

internal class PrinceOfVersionsImpl(
    private val checkForUpdatesUseCase: CheckForUpdatesUseCase<String>,
) : PrinceOfVersions {
    override suspend fun checkForUpdates(source: Loader): UpdateResult =
        checkForUpdatesUseCase.checkForUpdates(source)
}

public suspend fun PrinceOfVersions.checkForUpdates(
    url: String,
    username: String?,
    password: String?,
    networkTimeout: Duration,
): UpdateResult = checkForUpdates(
    source = provideDefaultLoader(
        url = url,
        username = username,
        password = password,
        networkTimeout = networkTimeout,
    ),
)
