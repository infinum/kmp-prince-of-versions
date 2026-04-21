package com.infinum.princeofversions.java

import com.infinum.princeofversions.BaseUpdateResult
import com.infinum.princeofversions.PrinceOfVersions
import com.infinum.princeofversions.checkForUpdatesFromUrl
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlinx.coroutines.runBlocking

/**
 * This file contains convenience extension functions for [PrinceOfVersions] to support Java interoperability on a JVM platform.
 */

private val executorService = Executors.newCachedThreadPool()

/**
 * Checks for updates using a [JavaLoader] and notifies the result through the provided [UpdaterCallback].
 *
 * @param javaLoader The [JavaLoader] used to load the update resource.
 * @param callbackExecutor The [java.util.concurrent.Executor] on which to execute the callback methods.
 * @param callback The [UpdaterCallback] to notify the result.
 *
 * @return A [Future] that can be used to cancel the operation if needed.
 */
public fun PrinceOfVersions.checkForUpdates(
    javaLoader: JavaLoader,
    callbackExecutor: java.util.concurrent.Executor,
    callback: UpdaterCallback<String>,
): Future<*> = executorService.submit {
    try {
        val result = checkForUpdates(javaLoader)
        callbackExecutor.execute { callback.onSuccess(result) }
    } catch (interrupted: InterruptedException) {
        throw interrupted
    } catch (e: Throwable) {
        callbackExecutor.execute { callback.onError(e) }
    }
}

/**
 * Checks for updates from a specified URL and notifies the result through the provided [UpdaterCallback].
 *
 * @param url The URL from which to fetch the update configuration.
 * @param callbackExecutor The [java.util.concurrent.Executor] on which to execute the callback methods.
 * @param callback The [UpdaterCallback] to notify the result.
 *
 * @return A [Future] that can be used to cancel the operation if needed.
 */
public fun PrinceOfVersions.checkForUpdates(
    url: String,
    callbackExecutor: java.util.concurrent.Executor,
    callback: UpdaterCallback<String>,
): Future<*> = executorService.submit {
    try {
        val result = checkForUpdates(url)
        callbackExecutor.execute { callback.onSuccess(result) }
    } catch (interrupted: InterruptedException) {
        throw interrupted
    } catch (e: Exception) {
        callbackExecutor.execute { callback.onError(e) }
    }
}

/**
 * Synchronously checks for updates from a specified URL.
 *
 * @param url The URL from which to fetch the update configuration.
 *
 * @return A [BaseUpdateResult] containing the result of the update check.
 */
public fun PrinceOfVersions.checkForUpdates(
    url: String,
): BaseUpdateResult<String> = runBlocking { checkForUpdatesFromUrl(url) }

/**
 * Synchronously checks for updates using a [JavaLoader].
 *
 * @param javaLoader The [JavaLoader] used to load the update resource.
 *
 * @return A [BaseUpdateResult] containing the result of the update check.
 */
public fun PrinceOfVersions.checkForUpdates(
    javaLoader: JavaLoader,
): BaseUpdateResult<String> = runBlocking { checkForUpdates { javaLoader.load() } }
