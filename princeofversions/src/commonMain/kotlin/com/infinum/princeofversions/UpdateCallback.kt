package com.infinum.princeofversions

/**
 * A callback interface used to deliver the result of an update check.
 *
 * Implement this interface to handle the success, failure, or no-update scenarios
 * of a Prince of Versions check.
 */
public interface UpdateCallback {

    /**
     * Called when a new update is available.
     *
     * @param version The version string of the new update.
     * @param isMandatory True if the update is mandatory, false otherwise.
     * @param metadata A map of metadata associated with the update.
     */

    public fun onNewUpdate(version: String, isMandatory: Boolean, metadata: Map<String, String>)

    /**
     * Called when no new update is available for the user.
     *
     * @param metadata A map of metadata from the configuration, which may still be useful
     * even if there is no update.
     */
    public fun onNoUpdate(metadata: Map<String, String>)

    /**
     * Called when an error occurs during the update check.
     *
     * @param error The [Throwable] that occurred.
     */
    public fun onError(error: Throwable)
}
