package com.infinum.princeofversions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


/**
 * Represents the main entry point for using the library.
 *
 * This library checks for application updates by fetching a configuration from a given source.
 * It supports asynchronous checks via callbacks and a more advanced call-based API for both
 * synchronous and asynchronous execution.
 *
 * ### Asynchronous Usage
 * The most common way to check for updates is asynchronously using [checkForUpdates].
 * This function takes a [ConfigSource] and an [UpdateCallback] and immediately
 * returns a [Cancelable] handle. The check is performed on a background coroutine dispatcher.
 *
 * ```kotlin
 * // On Android, for example:
 * val updater = PrinceOfVersions(context)
 *
 * val source = object : ConfigSource {
 *      override suspend fun load(): String {
 *          // Your implementation for fetching the config, e.g., from a network URL
 *          return myNetworkClient.fetch("[http://example.com/update.json](http://example.com/update.json)")
 *      }
 * }
 *
 * val callback = object : UpdateCallback {
 *      override fun onNewUpdate(version: String, isMandatory: Boolean, metadata: Map<String, String>) {
 *          // A new update is available
 *      }
 *      override fun onNoUpdate(metadata: Map<String, String>) {
 *          // No new update is available
 *      }
 *      override fun onError(error: Throwable) {
 *          // An error occurred
 *      }
 * }
 *
 * val cancelable = updater.checkForUpdates(source, callback)
 *
 * // To cancel the check:
 * cancelable.cancel()
 * ```
 *
 * ### Synchronous and Advanced Usage
 * For more control, use the [newCall] method. It returns a [PrinceOfVersionsCall]
 * which can be executed synchronously (`execute()`) or asynchronously (`enqueue()`).
 *
 * @see PrinceOfVersionsCall for synchronous and advanced asynchronous patterns.
 */
public expect class PrinceOfVersions {

    /**
     * Starts an asynchronous check for an update.
     *
     * The check is performed on the provided [dispatcher] and the result is delivered
     * to the [callback] on the main thread.
     *
     * @param source The source from which to load the update configuration (e.g., network URL).
     * @param callback The callback to notify with the update result or an error.
     * @param dispatcher The [CoroutineDispatcher] on which to perform the check. Defaults to [Dispatchers.Default].
     * @return A [Cancelable] handle that can be used to cancel the ongoing check.
     */
    public fun checkForUpdates(
        source: ConfigSource,
        callback: UpdateCallback,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): Cancelable

    /**
     * Creates a new call object for checking for an update from the given [source].
     *
     * The returned [PrinceOfVersionsCall] can be used to execute the check synchronously
     * or enqueue it for asynchronous execution.
     *
     * @param source The source from which to load the update configuration.
     * @return A new [PrinceOfVersionsCall] instance.
     */
    public fun newCall(
        source: ConfigSource
    ): PrinceOfVersionsCall
}
