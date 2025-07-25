package com.infinum.princeofversions

/**
 * Represents a single, prepared call to check for an update.
 *
 * An instance of this class is a one-shot object; it can be used only once.
 * To perform another check, a new instance must be created via [PrinceOfVersions.newCall].
 *
 * This interface provides two ways to perform the update check:
 * - [enqueue] for an asynchronous, callback-based approach.
 * - [execute] for a synchronous-style, coroutine-based approach.
 */
public interface PrinceOfVersionsCall {

    /**
     * Schedules the update check to be executed asynchronously.
     *
     * The check will be performed on a background thread, and the result will be
     * delivered to the provided [callback] on the main thread.
     *
     * @param callback The callback to notify with the update result or an error.
     * @return A [Cancelable] handle that can be used to cancel the ongoing check.
     *
     * ### Example, On Android:
     * ```kotlin
     * val call = princeOfVersions.newCall(source)
     * val cancelable = call.enqueue(object : UpdateCallback {
     *      override fun onNewUpdate(version: String, isMandatory: Boolean, metadata: Map<String, String>) {
     *          // A new update is available
     *      }
     *      override fun onNoUpdate(metadata: Map<String, String>) {
     *          // No new update is available
     *      }
     *      override fun onError(error: Throwable) {
     *          // An error occurred
     *      }
     * })
     * ```
     */
    public fun enqueue(callback: UpdateCallback): Cancelable

    /**
     * Executes the update check in a sequential manner using coroutines.
     *
     * This is a suspend function, meaning it performs its work asynchronously without
     * blocking the calling thread. From the caller’s perspective, it behaves like
     * sequential code that returns a result or throws an exception.
     *
     * On platforms that support coroutines (like Android), this should be called
     * from a coroutine scope. On platforms that don't (like iOS), the KMP compiler
     * will expose this as a function with a completion handler.
     *
     * @return The [UpdateResult] of the check.
     * @throws Throwable if the check fails for any reason.
     *
     * #### Example, on Android:
     * ```kotlin
     * CoroutineScope(Dispatchers.IO).launch {
     *      try {
     *          val result = princeOfVersions.newCall(source).execute()
     *          // Handle the successful result
     *      } catch (e: Throwable) {
     *          // Handle the error
     *      }
     * }
     * ```
     */
    public suspend fun execute(): UpdateResult
}
