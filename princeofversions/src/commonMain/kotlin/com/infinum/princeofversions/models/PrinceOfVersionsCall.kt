package com.infinum.princeofversions.models

/**
 * Represents a single, prepared call to check for an update.
 *
 * An instance of this class is a one-shot object; it can be used only once.
 * To perform another check, a new instance must be created via [com.infinum.princeofversions.PrinceOfVersions.newCall].
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
     */
    public fun enqueue(callback: UpdateCallback): Cancelable

    /**
     * Executes the update check.
     *
     * @return The [UpdateResult] of the check.
     *
     */
    public suspend fun execute(): UpdateResult
}
