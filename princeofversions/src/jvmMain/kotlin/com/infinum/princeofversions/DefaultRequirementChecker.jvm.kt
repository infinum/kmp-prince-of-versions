package com.infinum.princeofversions

/**
 * The default JVM [RequirementChecker] implementation.
 */
internal actual class DefaultRequirementChecker : RequirementChecker {

    /**
     * Checks if the current JVM version meets the required minimum version.
     *
     * @param value The minimum required JVM version as a String (e.g., "8", "11").
     * @return true if the current JVM version is greater than or equal to the required version.
     * @throws IllegalArgumentException if the required version from the JSON is not a valid integer,
     * or if the system's JVM version string is malformed.
     */
    actual override fun checkRequirements(value: String): Boolean {
        val requiredVersion = value.toIntOrNull()
            ?: throw IllegalArgumentException("Required JVM version '$value' is not a valid integer.")

        val currentVersion = getJavaVersion()
        return currentVersion >= requiredVersion
    }

    /**
     * Parses the major version from the "java.version" system property.
     * Handles both legacy ("1.X") and modern ("X.Y.Z") version string formats.
     *
     * @return The major version of the current JVM.
     * @throws IllegalArgumentException if the JVM version string is null or malformed.
     */
    private fun getJavaVersion(): Int {
        val version = System.getProperty("java.version")
        require(!version.isNullOrBlank()) { "JVM version string cannot be null or blank." }

        val parts = version.split('.')
        val firstPart = parts.first().toIntOrNull()
            ?: throw IllegalArgumentException("Malformed JVM version string: '$version'")

        return if (firstPart == 1) {
            // Legacy format: "1.8.0_292" -> 8
            parts.getOrNull(1)?.toIntOrNull()
                ?: throw IllegalArgumentException("Malformed legacy JVM version string: '$version'")
        } else {
            // Modern format: "11.0.11" -> 11
            firstPart
        }
    }

    companion object {
        const val KEY = "required_jvm_version"
    }
}
