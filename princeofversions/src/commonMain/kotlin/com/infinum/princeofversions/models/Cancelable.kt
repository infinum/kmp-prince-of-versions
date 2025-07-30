package com.infinum.princeofversions.models

/**
 * Represents a handle to an ongoing asynchronous operation that can be canceled.
 *
 * This provides a common mechanism to signal cancellation to a task that may be
 * running in the background.
 */
public interface Cancelable {

    /**
     * Attempts to cancel the ongoing operation.
     *
     * Calling this method signals that the operation should be aborted.
     * It is a best-effort cancellation and may not have an effect if the
     * operation has already completed or is in a non-cancellable state.
     */
    public fun cancel()
}
