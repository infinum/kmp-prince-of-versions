package com.infinum.princeofversions

/**
 * Represents the final status of an update check.
 */
public enum class UpdateStatus {
    /**
     * Indicates that no new update is available.
     */
    NO_UPDATE,

    /**
     * Indicates that a new, optional update is available.
     */
    OPTIONAL,

    /**
     * Indicates that a new, mandatory update is available.
     */
    MANDATORY,
}
