package com.infinum.princeofversions

/**
 * This class handles requirement checking while JSON parsing
 */
public fun interface RequirementChecker {
    /**
     * This method is used to check if passed data requires specified requirements
     *
     * @param value Value of the requirement we are checking
     * @return true or false depending if the data matched requirements
     * @throws Throwable in case of any error in parsing
     */
    @Throws(Throwable::class)
    public fun checkRequirements(value: String): Boolean
}
